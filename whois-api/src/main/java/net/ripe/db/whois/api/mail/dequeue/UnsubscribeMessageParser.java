package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UnsubscribeMessageParser {

    private static final ContentType TEXT_PLAIN = contentType("text/plain");

    private static final Pattern UNSUBSCRIBE_PATTERN = Pattern.compile("Unsubscribe (.*)");

    private final boolean enabled;

    @Autowired
    public UnsubscribeMessageParser(@Value("${mail.smtp.from:}") final String smtpFrom) {
        enabled = !StringUtils.isEmpty(smtpFrom);
    }

    @Nullable
    public EmailMessageInfo parse(final MimeMessage message) throws MessagingException, MailParsingException {
        if (!enabled || !isTextPlain(message)){
            return null;
        }

        try {
            final String messageId = getMessageIdFromSubject(message);
            final String from = getFrom(message);
            if ((messageId != null) && (from != null)) {
                return new EmailMessageInfo(List.of(from), messageId, null);
            }
        } catch (MessagingException | IllegalStateException ex){
            throw new MailParsingException("Error parsing text plain unsubscribe message");
        }

        return null;
    }

    private boolean isTextPlain(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return TEXT_PLAIN.match(contentType);
    }

    @Nullable
    private String getMessageIdFromSubject(final MimeMessage message) throws MessagingException {
        final String subject = message.getSubject();
        if (subject != null) {
            final Matcher matcher = UNSUBSCRIBE_PATTERN.matcher(subject);
            if (matcher.matches()) {
                return getAddress(matcher.group(1));
            }
        }
        return null;
    }

    private String getFrom(final MimeMessage message) throws MessagingException {
        final Address[] from = message.getFrom();
        if (from != null && from.length == 1 && (from[0] instanceof InternetAddress)) {
            return ((InternetAddress) from[0]).getAddress();
        }
        return null;
    }

    private static String getAddress(final String address) {
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
