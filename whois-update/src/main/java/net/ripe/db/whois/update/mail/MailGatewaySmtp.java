package net.ripe.db.whois.update.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.mail.BounceListener;
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

    private final BounceListener bouncedListener;

    @Value("${mail.smtp.enabled}")
    private boolean outgoingMailEnabled;

    @Value("${mail.smtp.retrySending:true}")
    private boolean retrySending;


    @Autowired
    public MailGatewaySmtp(final LoggerContext loggerContext, final MailConfiguration mailConfiguration,
                           final JavaMailSender mailSender, final BounceListener bouncedListener) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
        this.bouncedListener = bouncedListener;

        //setupBeforeSendListener();
        //setupReturnMailListener();
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

            if (bouncedListener.checkBounced(to)){
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

                loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
            });
        } catch (MailSendException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Caught %s: %s", e.getClass().getName(), e.getMessage()));
            LOGGER.error(String.format("Unable to send mail message to: %s", to), e);

            if (retrySending && !bouncedListener.checkBounced(to)) {
                throw e;
            } else {
                loggerContext.log(new Message(Messages.Type.ERROR, "Not retrying sending mail to %s with subject %s", to, subject));
            }
        }
    }

    /*private void setupBeforeSendListener() {
        this.mailSender.addEmailSendListener(new CustomJavaMailSender.EmailSendListener() {
            @Override
            public void beforeSend(MimeMessage message) {
                try {
                    bouncedListener.saveMessageId(getMessageIdFromHeader(message), getClientAddress(message));
                } catch (MessagingException e) {
                    LOGGER.error("Error processing not delivered message");
                }
            }
        });
    }*/

    private void setupReturnMailListener(){
        this.bouncedListener.createListener(this.mailSender, this.mailConfiguration.getFrom());
    }

    private String getMessageIdFromHeader(final MimeMessage message) throws MessagingException {
        final String[] messageIds = message.getHeader(MESSAGE_ID_HEADER);
        if (messageIds.length > 1){
            LOGGER.error("This is a single mail sender service, this shouldn't happen");
        }
        return messageIds[0];
    }

    private String getClientAddress(final MimeMessage mimeMessage) throws MessagingException {
        final Address[] addresses = mimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO);
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        return addresses[0].toString(); // Assuming there's only one address
    }
}
