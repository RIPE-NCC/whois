package net.ripe.db.whois.update.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailGatewaySmtp implements MailGateway {
    private static final Pattern INVALID_EMAIL_PATTERN = Pattern.compile("(?i)((?:auto|test)\\-dbm@ripe\\.net)");
    private static final Logger LOGGER = LoggerFactory.getLogger(MailGatewaySmtp.class);

    private final LoggerContext loggerContext;
    private final MailConfiguration mailConfiguration;
    private final JavaMailSender mailSender;
    private final UndeliverableMailDao undeliverableMailDao;
    private final OutgoingMessageDao outgoingMessageDao;

    @Value("${mail.app.path}")
    private String webRestPath;
    @Value("${mail.smtp.enabled:false}")
    private boolean outgoingMailEnabled;
    @Value("${mail.smtp.retrySending:true}")
    private boolean retrySending;

    @Autowired
    public MailGatewaySmtp(
            final LoggerContext loggerContext,
            final MailConfiguration mailConfiguration,
            final JavaMailSender mailSender,
            final UndeliverableMailDao undeliverableMailDao,
            final OutgoingMessageDao outgoingMessageDao) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
        this.undeliverableMailDao = undeliverableMailDao;
        this.outgoingMessageDao = outgoingMessageDao;
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

            //TODO acknowledgment should be sent even if the user is unsubscribe
            if (undeliverableMailDao.isUndeliverable(extractContentBetweenAngleBrackets(to))) {
                LOGGER.debug("" +
                        "Email appears in undeliverable list\n" +
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
            final MimeMessage mimeMessage = mailSender.createMimeMessage();

            mimeMessage.addHeader("Precedence", "bulk");
            mimeMessage.addHeader("Auto-Submitted", "auto-generated");
            mimeMessage.addHeader("List-Unsubscribe", String.format("<https://%s/db-web-ui/unsubscribe/%s>", webRestPath, mimeMessage.getMessageID()));
            mimeMessage.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");

            helper.setFrom(mailConfiguration.getFrom());

            final String punyCodedTo = PunycodeConversion.toAscii(to);
            final String punyCodedReplyTo = ! StringUtils.isEmpty(replyTo) ? PunycodeConversion.toAscii(replyTo) : "";
            helper.setTo(punyCodedTo);
            if (! StringUtils.isEmpty(punyCodedReplyTo)) {
                helper.setReplyTo(punyCodedReplyTo);
            }
            helper.setSubject(subject);
            helper.setText(text);

            loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
            mailSender.send(mimeMessage);

            outgoingMessageDao.saveOutGoingMessageId(
                extractContentBetweenAngleBrackets(mimeMessage.getMessageID()),
                extractContentBetweenAngleBrackets(punyCodedTo));

        } catch (MailSendException | MessagingException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Caught %s: %s", e.getClass().getName(), e.getMessage()));
            LOGGER.error(String.format("Unable to send mail message to: %s", to), e);
            //TODO acknowledgment should be sent even if the user is unsubscribe
            if (retrySending && !undeliverableMailDao.isUndeliverable(extractContentBetweenAngleBrackets(to))) {
                throw new MailSendException("Caught " + e.getClass().getName(), e);
            } else {
                loggerContext.log(new Message(Messages.Type.ERROR, "Not retrying sending mail to %s with subject %s", to, subject));
            }
        }
    }

    private String extractContentBetweenAngleBrackets(final String content) {
        if(content == null){
            return null;
        }

        try {
            //Message-ID is in address format rfc2822
            return new InternetAddress(content).getAddress();
        } catch (AddressException e) {
            return content;
        }
    }
}
