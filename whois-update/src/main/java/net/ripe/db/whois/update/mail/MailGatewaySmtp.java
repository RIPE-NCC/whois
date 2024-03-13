package net.ripe.db.whois.update.mail;

import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
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

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailGatewaySmtp implements MailGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailGatewaySmtp.class);

    private static final Pattern INVALID_EMAIL_PATTERN = Pattern.compile("(?i)((?:auto|test)\\-dbm@ripe\\.net)");

    private final LoggerContext loggerContext;
    private final MailConfiguration mailConfiguration;
    private final JavaMailSender mailSender;
    private final EmailStatusDao emailStatusDao;
    private final OutgoingMessageDao outgoingMessageDao;

    @Value("${web.baseurl}")
    private String webRestPath;

    @Autowired
    public MailGatewaySmtp(
            final LoggerContext loggerContext,
            final MailConfiguration mailConfiguration,
            final JavaMailSender mailSender,
            final EmailStatusDao emailStatusDao,
            final OutgoingMessageDao outgoingMessageDao) {
        this.loggerContext = loggerContext;
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
        this.emailStatusDao = emailStatusDao;
        this.outgoingMessageDao = outgoingMessageDao;
    }

    @Override
    public void sendEmail(final String to, final ResponseMessage responseMessage) {
        sendEmail(to, responseMessage.getSubject(), responseMessage.getMessage(), responseMessage.getReplyTo());
    }

    @Override
    public void sendEmail(final String to, final String subject, final String text, @Nullable final String replyTo) {
        if (! mailConfiguration.isEnabled()) {
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
        if (emailStatusDao.canNotSendEmail(extractEmailBetweenAngleBrackets(to))) {
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
            LOGGER.error("Caught MailException", e);
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to send mail to %s with subject %s", to, subject), e);
        } catch (Exception e) {
            LOGGER.error("Caught", e);
            throw e;
        }
    }

    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    private void sendEmailAttempt(final String to, final String replyTo, final String subject, final String text) {
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            setHeaders(mimeMessage);

            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");
            helper.setFrom(mailConfiguration.getFrom());

            final String punyCodedTo = PunycodeConversion.toAscii(to);
            helper.setTo(punyCodedTo);

            if (!Strings.isNullOrEmpty(replyTo)){
                helper.setReplyTo(PunycodeConversion.toAscii(replyTo));
            }

            helper.setSubject(subject);
            helper.setText(text);

            loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
            mailSender.send(mimeMessage);
            storeAsOutGoingMessage(mimeMessage, punyCodedTo);

        } catch (MailSendException | MessagingException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Caught %s: %s", e.getClass().getName(), e.getMessage()));
            LOGGER.error(String.format("Unable to send mail message to: %s", to), e);
            //TODO acknowledgment should be sent even if the user is unsubscribe
            if (mailConfiguration.retrySending() && !emailStatusDao.canNotSendEmail(extractEmailBetweenAngleBrackets(to))) {
                throw new MailSendException("Caught " + e.getClass().getName(), e);
            } else {
                loggerContext.log(new Message(Messages.Type.ERROR, "Not retrying sending mail to %s with subject %s", to, subject));
            }
        }
    }

    private void storeAsOutGoingMessage(MimeMessage mimeMessage, String punyCodedTo) throws MessagingException {
        outgoingMessageDao.saveOutGoingMessageId(extractEmailBetweenAngleBrackets(mimeMessage.getMessageID()),   //Message-ID is in rfc2822 address format
                extractEmailBetweenAngleBrackets(punyCodedTo));
    }

    private void setHeaders(MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.addHeader("Precedence", "bulk");
        mimeMessage.addHeader("Auto-Submitted", "auto-generated");
        if (!Strings.isNullOrEmpty(mailConfiguration.getSmtpFrom())) {
            mimeMessage.addHeader("List-Unsubscribe",
                String.format("<%s/unsubscribe/%s>, <mailto:%s?subject=Unsubscribe%%20%s>",
                webRestPath,
                mimeMessage.getMessageID(),
                mailConfiguration.getSmtpFrom(),
                mimeMessage.getMessageID()));
            mimeMessage.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }
    }

    @Nullable
    private String extractEmailBetweenAngleBrackets(final String email) {
        if(email == null) {
            return null;
        }

        try {
            return new InternetAddress(email).getAddress();
        } catch (AddressException e) {
            return email;
        }
    }
}
