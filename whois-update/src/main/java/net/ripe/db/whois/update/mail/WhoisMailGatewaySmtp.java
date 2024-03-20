package net.ripe.db.whois.update.mail;

import jakarta.mail.MessagingException;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.mail.EmailStatus;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

@Component
public class WhoisMailGatewaySmtp extends MailGatewaySmtp {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisMailGatewaySmtp.class);

    private final LoggerContext loggerContext;


    @Autowired
    public WhoisMailGatewaySmtp(
            final LoggerContext loggerContext,
            final MailConfiguration mailConfiguration,
            final JavaMailSender mailSender,
            final EmailStatusDao emailStatusDao,
            final OutgoingMessageDao outgoingMessageDao,
            @Value("${web.baseurl}") final String webBaseUrl) {
        super(mailConfiguration, mailSender, emailStatusDao, outgoingMessageDao, webBaseUrl);
        this.loggerContext = loggerContext;
    }

    @Override
    public void sendEmail(final String to, final ResponseMessage responseMessage) {
        sendEmail(to, responseMessage.getSubject(), responseMessage.getMessage(), responseMessage.getReplyTo());
    }

    @Override
    public void sendEmail(final String to, final String subject, final String text, @Nullable final String replyTo) {
        if (!mailConfiguration.isEnabled()) {
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
        if (canNotSendEmail(extractEmailBetweenAngleBrackets(to))) {
            LOGGER.debug("" +
                    "Email appears in undeliverable/unsubscribed list\n" +
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
            LOGGER.error("Caught MailException", e);
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to send mail to %s with subject %s", to, subject), e);
        } catch (Exception e) {
            LOGGER.error("Caught", e);
            throw e;
        }
    }

    @Override
    public void sendEmail(final Set<String> to, final String subject, final String text, final String replyTo, final boolean html) {
        throw new NotImplementedException("No implemented by whois, not multiple recipients are used in Whois messages");
    }


    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    private void sendEmailAttempt(final String recipient, final String replyTo, final String subject, final String text) {
        try {
            super.sendEmailAttempt(Set.of(recipient), replyTo, subject, text, false, loggerContext);
        } catch (MailSendException | MessagingException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Caught %s: %s", e.getClass().getName(), e.getMessage()));
            LOGGER.error(String.format("Unable to send mail message to: %s", recipient), e);

            if (mailConfiguration.retrySending()) {
                throw new MailSendException("Caught " + e.getClass().getName(), e);
            } else {
                loggerContext.log(new Message(Messages.Type.ERROR, "Not retrying sending mail to %s with subject %s", recipient, subject));
            }
        }
    }

    @Override
    public boolean canNotSendEmail(final String emailAddresses) {
        final Map<String, EmailStatus> emailStatus = emailStatusDao.getEmailStatus(Set.of(emailAddresses));
        return !emailStatus.isEmpty();
    }
}