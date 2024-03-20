package net.ripe.db.whois.update.mail;

import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class MailGatewaySmtp implements MailGateway {

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

    @Override
    public abstract void sendEmail(final String to, final ResponseMessage responseMessage);

    @Override
    public abstract void sendEmail(final String to, final String subject, final String text, @Nullable final String replyTo);

    @Override
    public abstract void sendEmail(final Set<String> to, final String subject, final String text, final String replyTo, boolean html);

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

    protected void sendEmailAttempt(final Set<String> recipients, final String replyTo, final String subject,
                                    final String text, final boolean html, final LoggerContext loggerContext) throws MessagingException {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        setHeaders(mimeMessage);

        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, "UTF-8");
        helper.setFrom(mailConfiguration.getFrom());

        final String[] recipientsPunycode = recipients.stream().map(PunycodeConversion::toAscii).distinct().toArray(String[]::new);
        helper.setTo(recipientsPunycode);

        if (!Strings.isNullOrEmpty(replyTo)){
            helper.setReplyTo(PunycodeConversion.toAscii(replyTo));
        }

        helper.setSubject(subject);
        helper.setText(text, html);

        if (loggerContext != null) {
            loggerContext.log("msg-out.txt", new MailMessageLogCallback(mimeMessage));
        }

        mailSender.send(mimeMessage);

        final String messageId = mimeMessage.getMessageID();
        Arrays.stream(recipientsPunycode).forEach(punyCodeTo -> storeAsOutGoingMessage(messageId, punyCodeTo));
    }

    private void storeAsOutGoingMessage(final String mimeMessageId, final String punyCodedTo){
        outgoingMessageDao.saveOutGoingMessageId(extractEmailBetweenAngleBrackets(mimeMessageId),   //Message-ID is in rfc2822 address format
                extractEmailBetweenAngleBrackets(punyCodedTo));
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
}
