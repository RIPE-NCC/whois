package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.aspects.RetryFor;
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
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
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

    @Value("${mail.smtp.enabled}")
    private boolean outgoingMailEnabled;

    @Value("${mail.smtp.retrySending:true}")
    private boolean retrySending;

    @Autowired
    public MailGatewaySmtp(final LoggerContext loggerContext, final MailConfiguration mailConfiguration, final JavaMailSender mailSender) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
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

        try {
            final Matcher matcher = INVALID_EMAIL_PATTERN.matcher(to);
            if (matcher.find()) {
                throw new MailSendException("Refusing outgoing email: " + text);
            }

            sendEmailAttempt(to, replyTo, subject, text);
        } catch (MailException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to send mail to {} with subject {}", to, subject), e);
            LOGGER.error("Unable to send mail message to: {}", to, e);
        }
    }

    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    private void sendEmailAttempt(final String to, final String replyTo, final String subject, final String text) {
        try {
            mailSender.send(new MimeMessagePreparator() {
                @Override
                public void prepare(final MimeMessage mimeMessage) throws MessagingException {
                    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");
                    message.setFrom(mailConfiguration.getFrom());
                    message.setTo(to);
                    if (!StringUtils.isEmpty(replyTo))
                        message.setReplyTo(replyTo);
                    message.setSubject(subject);
                    message.setText(text);

                    mimeMessage.addHeader("Precedence", "bulk");
                    mimeMessage.addHeader("Auto-Submitted", "auto-generated");

                    loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
                }
            });
        } catch (MailSendException e) {
            if (retrySending) {
                throw e;
            } else {
                LOGGER.info("not retrying sending mail to {}", to);
            }
        }
    }
}
