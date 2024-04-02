package net.ripe.db.whois.update.mail;

import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MailGatewaySmtp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailGatewaySmtp.class);
    protected static final Pattern INVALID_EMAIL_PATTERN = Pattern.compile("(?i)((?:auto|test)\\-dbm@ripe\\.net)");

    protected final MailConfiguration mailConfiguration;
    protected final JavaMailSender mailSender;
    protected final EmailStatusDao emailStatusDao;
    protected final OutgoingMessageDao outgoingMessageDao;
    protected final String webBaseUrl;


    public MailGatewaySmtp(
            final MailConfiguration mailConfiguration,
            final JavaMailSender mailSender,
            final EmailStatusDao emailStatusDao,
            final OutgoingMessageDao outgoingMessageDao,
            @Value("${web.baseurl}") final String webBaseUrl) {
        this.mailConfiguration = mailConfiguration;
        this.mailSender = mailSender;
        this.emailStatusDao = emailStatusDao;
        this.outgoingMessageDao = outgoingMessageDao;
        this.webBaseUrl = webBaseUrl;
    }

    protected abstract boolean canNotSendEmail(final String emailAddresses);

    @Nullable
    protected String extractEmailBetweenAngleBrackets(final String email) {
        if(email == null) {
            return null;
        }

        try {
            return new InternetAddress(email).getAddress();
        } catch (AddressException e) {
            return email;
        }
    }

    protected MimeMessage sendEmailAttempt(final Set<String> recipients, final String replyTo, final String subject, final String text, final boolean html) throws MessagingException {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");

        return sendEmailAttempt(helper, recipients, replyTo, subject, text, html);
    }

    protected MimeMessage sendEmailAttempt(final MimeMessageHelper helper, final Set<String> recipients, final String replyTo, final String subject, final String text, final boolean html) throws MessagingException {
        return setCommonConfigurationAndSend(helper, recipients, replyTo, subject, text, html);
    }

    protected boolean canSendEmail(final Set<String> to, final String replyTo, final String subject, final String text){
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

            return false;
        }

        final List<String> availableEmails = getWellFormattedDeliverableEmails(to, subject, text, replyTo);

        if (availableEmails.isEmpty()){
            LOGGER.debug("No available well formatted emails");
            return false;
        }
        return true;
    }

    private MimeMessage setCommonConfigurationAndSend(final MimeMessageHelper helper, final Set<String> recipients, final String replyTo,
                                                      final String subject, final String text, final boolean html) throws MessagingException {
        helper.setFrom(mailConfiguration.getFrom());

        final String[] recipientsPunycode = recipients.stream().map(PunycodeConversion::toAscii).distinct().toArray(String[]::new);
        helper.setTo(recipientsPunycode);

        if (!Strings.isNullOrEmpty(replyTo)){
            helper.setReplyTo(PunycodeConversion.toAscii(replyTo));
        }

        helper.setSubject(subject);
        helper.setText(text, html);

        final MimeMessage message = helper.getMimeMessage();
        setHeaders(message);

        mailSender.send(message);

        persistOutGoingMessageInfo(message, recipientsPunycode);

        return message;
    }


    private void setHeaders(MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.addHeader("Precedence", "bulk");
        mimeMessage.addHeader("Auto-Submitted", "auto-generated");
        if (!Strings.isNullOrEmpty(mailConfiguration.getSmtpFrom())) {
            mimeMessage.addHeader("List-Unsubscribe",
                    String.format("<%s%sapi/unsubscribe/%s>, <mailto:%s?subject=Unsubscribe%%20%s>",
                            webBaseUrl,
                            (webBaseUrl.endsWith("/") ? "" : "/"),
                            mimeMessage.getMessageID(),
                            mailConfiguration.getSmtpFrom(),
                            mimeMessage.getMessageID()));
            mimeMessage.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }
    }

    private void storeAsOutGoingMessage(final String mimeMessageId, final String punyCodedTo){
        outgoingMessageDao.saveOutGoingMessageId(extractEmailBetweenAngleBrackets(mimeMessageId),   //Message-ID is in rfc2822 address format
                extractEmailBetweenAngleBrackets(punyCodedTo));
    }

    private List<String> getWellFormattedDeliverableEmails(final Set<String> to, final String subject,
                                                           final String text, final String replyTo) {
        return to.stream().filter(email -> {
            //TODO acknowledgment should be sent even if the user is unsubscribe
            if (canNotSendEmail(extractEmailBetweenAngleBrackets(email))){
                LOGGER.debug("" +
                        "Email appears in undeliverable/unsubscribed list\n" +
                        "\n" +
                        "to      : {}\n" +
                        "reply-to : {}\n" +
                        "subject : {}\n" +
                        "\n" +
                        "{}\n" +
                        "\n" +
                        "\n", email, replyTo, subject, text);
                return false;
            }
            final Matcher matcher = INVALID_EMAIL_PATTERN.matcher(email);
            if (matcher.find()){
                LOGGER.error("Email with incorrect pattern");
                return false;
            }
            return true;
        }).toList();
    }

    private void persistOutGoingMessageInfo(final MimeMessage mimeMessage, final String[] recipientsPunycode) throws MessagingException {
        final String messageId = mimeMessage.getMessageID();
        Arrays.stream(recipientsPunycode).forEach(punyCodeTo -> storeAsOutGoingMessage(messageId, punyCodeTo));
    }
}
