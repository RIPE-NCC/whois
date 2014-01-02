package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailGatewaySmtp implements MailGateway {
    private static final Pattern INVALID_EMAIL_PATTERN = Pattern.compile("(?i)((?:auto|test)\\-dbm@ripe\\.net)");
    private static final Logger LOGGER = LoggerFactory.getLogger(MailGatewaySmtp.class);

    private final LoggerContext loggerContext;
    private final MailConfiguration mailConfiguration;
    private final JavaMailSender mailSender;

    private boolean outgoingMailEnabled;

    @Value("${mail.smtp.enabled}")
    public void setOutgoingMailEnabled(final boolean outgoingMailEnabled) {
        LOGGER.info("Outgoing mail enabled: {}", outgoingMailEnabled);
        this.outgoingMailEnabled = outgoingMailEnabled;
    }

    @Autowired
    public MailGatewaySmtp(final LoggerContext loggerContext, final MailConfiguration mailConfiguration, final JavaMailSender mailSender) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(final String to, final ResponseMessage responseMessage) {
        sendEmail(to, responseMessage.getSubject(), responseMessage.getMessage());
    }

    @Override
    public void sendEmail(final String to, final String subject, final String text) {
            if (!outgoingMailEnabled) {
                LOGGER.debug("" +
                        "Outgoing mail disabled\n" +
                        "\n" +
                        "to      : {}\n" +
                        "subject : {}\n" +
                        "\n" +
                        "{}\n" +
                        "\n" +
                        "\n", to, subject, text);

                return;
            }

        try {
            final Matcher matcher = INVALID_EMAIL_PATTERN.matcher(to);
            if (matcher.find()) {
                throw new MailSendException("Refusing outgoing email: " + text);
            }

            sendEmailAttempt(to, subject, text);
        } catch (MailException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to send mail to {} with subject {}", to, subject), e);
            LOGGER.error("Unable to send mail message to: {}", to, e);
        }
    }

    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    private void sendEmailAttempt(final String to, final String subject, final String text) {
        mailSender.send(new MimeMessagePreparator() {
            @Override
            public void prepare(final MimeMessage mimeMessage) throws MessagingException {
                final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");
                message.setFrom(mailConfiguration.getFrom());
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);

                mimeMessage.addHeader("Precedence", "bulk");
                mimeMessage.addHeader("Auto-Submitted", "auto-generated");

                loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
            }
        });
    }
}
