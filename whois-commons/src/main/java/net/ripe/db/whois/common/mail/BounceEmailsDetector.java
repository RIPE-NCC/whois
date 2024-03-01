package net.ripe.db.whois.common.mail;

import io.netty.util.internal.StringUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimePart;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class BounceEmailsDetector {

    private final static Logger LOGGER = LoggerFactory.getLogger(BounceEmailsDetector.class);

    private static final String ERROR_REPORT_HEADER_VALUE = "multipart/report; report-type=delivery-status;";

    private final UndeliverableMailDao undeliverableMailDao;
    private final OutgoingMessageDao outgoingMessageDao;

    @Autowired
    public BounceEmailsDetector(
            final UndeliverableMailDao undeliverableMailDao,
            final OutgoingMessageDao outgoingMessageDao) {
        this.undeliverableMailDao = undeliverableMailDao;
        this.outgoingMessageDao = outgoingMessageDao;
    }

    public void checkMailBounced(final MimeMessage message) {
        if (!isUndeliveredReport(message)) {
            return;
        }

        final MessageInfo messageRelevantInformation = MessageInformationExtractor.parse(message);
        if (messageRelevantInformation == null || messageRelevantInformation.isInvalidMessage()) {
            return;
        }

        if (!"failed".equals(messageRelevantInformation.getAction())) {
            return;
        }

        if (messageRelevantInformation.getReturnPath() != null && !hasSenderAsReturnPath(messageRelevantInformation)) {
            return;
        }

        final String email = outgoingMessageDao.getEmail(messageRelevantInformation.getMessageId());
        if (StringUtil.isNullOrEmpty(email)){
            return;
        }

        undeliverableMailDao.createUndeliverableEmail(email);
    }

    private boolean isUndeliveredReport(final MimeMessage message) {
        try {
            return message.getHeader("Content-Type", null).contains(ERROR_REPORT_HEADER_VALUE);
        } catch (MessagingException ex){
            return false;
        }
    }

    private boolean hasSenderAsReturnPath(final MessageInfo messageRelevantInformation) {
        return MailUtil.extractContentBetweenAngleBrackets(messageRelevantInformation.getFrom())
                .equals(MailUtil.extractContentBetweenAngleBrackets(messageRelevantInformation.getReturnPath()));
    }

    private static class MessageInformationExtractor {

        private static final String BODY_DELIMITED = ":";

        protected static MessageInfo parse(final MimeMessage mimeMessage) {
            try{
                final List<MimePart> relevantParts = Lists.newArrayList();
                fillWithRelevantParts(mimeMessage, relevantParts);
                if (relevantParts.size() != 2){
                    return null;
                }
                return extractInformationFromParts(relevantParts);
            } catch (MessagingException | IOException ex) {
                //TODO: create a warn
                return null;
            }
        }

        private static MessageInfo extractInformationFromParts(final List<MimePart> mimePart) throws MessagingException, IOException {
            final MessageInfo messageInfo = new MessageInfo();
            for (final MimePart part: mimePart) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains(BODY_DELIMITED)){
                            final String[] splitLine = line.split(BODY_DELIMITED, 2);
                            switch (splitLine[0]) {
                                case "Message-Id" -> messageInfo.setMessageId(splitLine[1].trim());
                                case "From" -> messageInfo.setFrom(splitLine[1].trim());
                                case "Return-path" -> messageInfo.setReturnPath(splitLine[1].trim());
                                case "Action" -> messageInfo.setAction(splitLine[1].trim());
                            }
                        }
                    }
                }
            }
            return messageInfo;
        }

        private static void fillWithRelevantParts(final Part part, final List<MimePart> mimeParts) throws MessagingException, IOException {
            if (isRfc822Part(part)) {
                mimeParts.add((MimePart) part);
            }

            if(part.isMimeType("message/delivery-status")){
                mimeParts.add((MimePart) part);
            }

            if (part.isMimeType("multipart/*")){
                final Multipart multipart = (Multipart) part.getContent();
                //TODO: we must securify this, the user can send a message with a million of multiParts. We need to
                // check what is the maximum parts that we can get in a normal message and return null in case
                // getCount() is higher that this value -> There is no fixed limit defined in the MIME specification (RFC 2046)
                for (int multiPartPosition = 0; multiPartPosition < multipart.getCount(); multiPartPosition++){
                    fillWithRelevantParts(multipart.getBodyPart(multiPartPosition), mimeParts);
                    if (mimeParts.size() == 2){
                        break;
                    }
                }
            }
        }

        private static boolean isRfc822Part(final Part part) throws MessagingException {
            final String contentType = part.getContentType();
            return contentType != null && (contentType.contains("rfc822"));
        }

    }

    private static class MessageInfo {
        private String messageId;
        private String from;
        private String returnPath;
        private String action;

        public String getAction() {
            return action;
        }

        public String getFrom() {
            return from;
        }
        public String getReturnPath() {
            return returnPath;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(final String messageId) {
            this.messageId = MailUtil.extractContentBetweenAngleBrackets(messageId);
        }

        public void setFrom(final String from) {
            this.from = MailUtil.extractContentBetweenAngleBrackets(from);
        }

        public void setReturnPath(final String returnPath) {
            this.returnPath = returnPath;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public boolean isInvalidMessage(){
            return this.messageId == null || this.from == null || this.action == null;      // TODO: returnPath null ?
        }
    }
}
