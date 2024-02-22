package net.ripe.db.whois.update.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Transport;
import jakarta.mail.event.TransportEvent;
import jakarta.mail.event.TransportListener;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.BouncedMailDao;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailGatewaySmtp implements MailGateway {
    private static final Pattern INVALID_EMAIL_PATTERN = Pattern.compile("(?i)((?:auto|test)\\-dbm@ripe\\.net)");
    private static final Logger LOGGER = LoggerFactory.getLogger(MailGatewaySmtp.class);

    private static final String MESSAGE_ID_HEADER = "Message-ID";
    private final LoggerContext loggerContext;
    private final MailConfiguration mailConfiguration;
    private final JavaMailSender mailSender;

    private final BouncedMailDao bouncedMailDao;

    @Value("${mail.smtp.enabled}")
    private boolean outgoingMailEnabled;

    @Value("${mail.smtp.retrySending:true}")
    private boolean retrySending;

    @Autowired
    public MailGatewaySmtp(final LoggerContext loggerContext, final MailConfiguration mailConfiguration,
                           final JavaMailSender mailSender, BouncedMailDao bouncedMailDao) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
        this.bouncedMailDao = bouncedMailDao;
        createListener();
    }

    @Override
    public void sendEmail(final String to, final ResponseMessage responseMessage) {
        sendEmail(to, responseMessage.getSubject(), responseMessage.getMessage(), responseMessage.getReplyTo());
    }

    @Override
    public void sendEmail(final String to, final String subject, final String text, @Nullable final String replyTo) {
            if (!outgoingMailEnabled) {
                LOGGER.debug("" +
                        "Outgoing mail disabled\n" +
                        "\n" +
                        "to      : {}\n" +
                        "reply-to : {}\n" +
                        "subject : {}\n" +
                        "\n" +
                        "{}\n" +
                        "\n" +
                        "\n", to, replyTo, subject, text);

                return;
            }

            if (bouncedMailDao.isBouncedEmail(to)){
                LOGGER.debug("" +
                        "Bounced email\n" +
                        "\n" +
                        "to      : {}\n" +
                        "reply-to : {}\n" +
                        "subject : {}\n" +
                        "\n" +
                        "{}\n" +
                        "\n" +
                        "\n", to, replyTo, subject, text);
                return;
            }

        try {
            final Matcher matcher = INVALID_EMAIL_PATTERN.matcher(to);
            if (matcher.find()) {
                throw new MailSendException("Refusing outgoing email: " + text);
            }
            sendEmailAttempt(to, replyTo, subject, text);
        } catch (MailException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to send mail to %s with subject %s", to, subject), e);
        }
    }

    private void createListener(){
        try {
            Transport transport = ((JavaMailSenderImpl) mailSender).getSession().getTransport();
            transport.addTransportListener(new TransportListener() {

                @Override
                public void messageDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing delivered message");
                    }
                }

                @Override
                public void messageNotDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        if (isValidMessage(message) && isBounced(message, transportEvent)) {
                            final String bouncedEmail = getClientAddress(message);
                            if (StringUtils.isEmpty(bouncedEmail)){
                                LOGGER.error("Not able to get the client address");
                                return;
                            }
                            bouncedMailDao.createBouncedEmail(bouncedEmail);
                            LOGGER.info("Bounced mail for {}", bouncedEmail);
                        }
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing not delivered message");
                    }
                }

                @Override
                public void messagePartiallyDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing partial delivered message");
                    }
                }
            });
        } catch (NoSuchProviderException e){
            LOGGER.error("Error creating the listener for incoming messages");
        }
    }

    private void cleanOnGoingMessage(final MimeMessage message) throws MessagingException {
        final String[] messageIds = message.getHeader(MESSAGE_ID_HEADER);
        if (messageIds.length > 1){
            LOGGER.error("This is a single mail sender service, this shouldn't happen");
        }
        bouncedMailDao.deleteOnGoingMessage(messageIds[0]);
    }

    private boolean isValidMessage(final MimeMessage message) throws MessagingException{
        final String[] messageIds = message.getHeader(MESSAGE_ID_HEADER);
        if (messageIds.length > 1){
            LOGGER.error("This is a single mail sender service, this shouldn't happen");
            return false;
        }
        final String messageId = messageIds[0];
        if (!bouncedMailDao.onGoingMessageExist(messageId)){
            LOGGER.error("Received email with messageId: {} which was not sent by us", messageId);
            return false;
        }
        return true;
    }

    private boolean isBounced(final MimeMessage message, final TransportEvent transportEvent) throws MessagingException{
        // Check if the failure is due to a bounce by inspecting invalid addresses and Return-Path header
        // If there are any invalid addresses associated with the transportEvent, it indicated that
        // the email failed to be delivered to those recipients. This indicates a potential bounce
        if (transportEvent.getInvalidAddresses() == null || transportEvent.getInvalidAddresses().length <= 0) {
            return false;
        }

        final String returnPath = message.getHeader("Return-Path", null);
        return returnPath != null && returnPath.contains(mailConfiguration.getFrom());
    }

    private String getClientAddress(final MimeMessage mimeMessage) throws MessagingException {
        final Address[] addresses = mimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO);
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        return addresses[0].toString(); // Assuming there's only one address
    }

    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    private void sendEmailAttempt(final String to, final String replyTo, final String subject, final String text) {
        try {
            mailSender.send(mimeMessage -> {
                final String punyCodedTo = PunycodeConversion.toAscii(to);
                final String puncyCodedReplyTo = !StringUtils.isEmpty(replyTo)? PunycodeConversion.toAscii(replyTo) : "";

                final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");
                message.setFrom(mailConfiguration.getFrom());
                message.setTo(punyCodedTo);
                if (!StringUtils.isEmpty(puncyCodedReplyTo)) {
                    message.setReplyTo(puncyCodedReplyTo);
                }
                message.setSubject(subject);
                message.setText(text);

                mimeMessage.addHeader("Precedence", "bulk");
                mimeMessage.addHeader("Auto-Submitted", "auto-generated");

                generateMessageId(mimeMessage, to);

                loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
            });
        } catch (MailSendException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Caught %s: %s", e.getClass().getName(), e.getMessage()));
            LOGGER.error(String.format("Unable to send mail message to: %s", to), e);

            if (retrySending && !bouncedMailDao.isBouncedEmail(to)) {
                throw e;
            } else {
                loggerContext.log(new Message(Messages.Type.ERROR, "Not retrying sending mail to %s with subject %s", to, subject));
            }
        }
    }

    private void generateMessageId(final MimeMessage mimeMessage, final String to) {
        try {
            final String uniqueId = "<" + System.currentTimeMillis() + "." + Math.random() + ">";
            mimeMessage.addHeader(MESSAGE_ID_HEADER, uniqueId);

            //TODO: [MH] Check if we really need to keep track of the ongoing messages?
            bouncedMailDao.createOnGoingMessageId(uniqueId, to);
        } catch (MessagingException ex){
            LOGGER.error("failing generating message Id");
        }
    }
}
