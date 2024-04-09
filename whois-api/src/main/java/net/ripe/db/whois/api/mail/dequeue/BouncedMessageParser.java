package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.common.QueryMessage;
import org.apache.commons.compress.utils.Lists;
import org.eclipse.angus.mail.dsn.DeliveryStatus;
import org.eclipse.angus.mail.dsn.MultipartReport;
import org.eclipse.angus.mail.dsn.Report;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BouncedMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BouncedMessageParser.class);

    private static final ContentType MULTIPART_REPORT = contentType("multipart/report");

    private final boolean enabled;

    private static final Pattern FINAL_RECIPIENT_MATCHER = Pattern.compile("^(rfc822;)(.+@.+$)");


    @Autowired
    public BouncedMessageParser(@Value("${mail.smtp.from:}") final String smtpFrom) {
        this.enabled = !Strings.isNullOrEmpty(smtpFrom);
    }

    @Nullable
    public EmailMessageInfo parse(final MimeMessage message) throws MessagingException, MailParsingException {
        if (!enabled || !isMultipartReport(message) ){
            return null;
        }

        try {
            final MultipartReport multipartReport = multipartReport(message.getContent());
            if (isReportDeliveryStatus(multipartReport)) {
                final DeliveryStatus deliveryStatus = deliveryStatus(message);
                if (isFailed(deliveryStatus)) {
                    final MimeMessage returnedMessage = multipartReport.getReturnedMessage();
                    final String messageId = getMessageId(returnedMessage.getMessageID());
                    final List<String> recipient = extractRecipients(deliveryStatus);
                    return new EmailMessageInfo(recipient, messageId);
                }
            }
        } catch (MessagingException | IOException ex){
            throw new MailParsingException("Error parsing multipart report");
        }
        throw new MailParsingException("MultiPart message without failure report");
    }

    private boolean isMultipartReport(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return MULTIPART_REPORT.match(contentType);
    }

    private boolean isReportDeliveryStatus(final MultipartReport multipartReport) throws MessagingException {
        final Report report = multipartReport.getReport();
        return ("delivery-status".equals(report.getType()));
    }

    private MultipartReport multipartReport(final Object content) throws MessagingException {
        if (content instanceof MultipartReport) {
            return (MultipartReport)content;
        } else {
            throw new MessagingException("Unexpected content was not multipart/report");
        }
    }

    private DeliveryStatus deliveryStatus(final Part part) throws MessagingException {
        try {
            return new DeliveryStatus(part.getInputStream());
        } catch (MessagingException | IOException e) {
            throw new MessagingException("Unexpected error parsing message/delivery-status part", e);
        }
    }

    private List<String> extractRecipients(final DeliveryStatus deliveryStatus) {
        final List<String> recipients = Lists.newArrayList();
        for (int dsn = 0; dsn < deliveryStatus.getRecipientDSNCount(); dsn++) {
            final String recipient = getHeaderValue(deliveryStatus.getRecipientDSN(dsn), "Final-Recipient");
            if (recipient == null){
                continue;
            }
            final Matcher finalRecipientMatcher = FINAL_RECIPIENT_MATCHER.matcher(recipient);
            if (!finalRecipientMatcher.matches() || finalRecipientMatcher.groupCount() != 2){
                LOGGER.error("Wrong formatted recipient {}", recipient);
                continue;
            }
            recipients.add(finalRecipientMatcher.group(2));
        }
        return recipients;
    }

    private boolean isFailed(final DeliveryStatus deliveryStatus) {
        for (int dsn = 0; dsn < deliveryStatus.getRecipientDSNCount(); dsn++) {
            if ("failed".equals(getHeaderValue(deliveryStatus.getRecipientDSN(dsn), "Action"))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private String getHeaderValue(final InternetHeaders headers, final String name) {
        final String[] values = headers.getHeader(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    private static String getMessageId(final String messageId) {
        if (messageId == null) {
            throw new IllegalStateException("No Message-Id header");
        }
        return getAddress(messageId);
    }

    private static String getAddress(final String address) {
        if (address == null) {
            throw new IllegalStateException("No address");
        }
        try {
            return new InternetAddress(address).getAddress();
        } catch (AddressException e) {
            throw new IllegalStateException("Address " + address, e);
        }
    }

    private static ContentType contentType(final String contentType) {
        if (contentType == null) {
            throw new IllegalStateException("No Content-Type");
        }
        try {
            return new ContentType(contentType);
        } catch (jakarta.mail.internet.ParseException e) {
            throw new IllegalStateException("Content-Type " + contentType, e);
        }
    }

}
