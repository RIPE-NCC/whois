package net.ripe.db.whois.common.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MailLog {

    private static final AttributeParser.EmailParser EMAIL_PARSER = new AttributeParser.EmailParser();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void info(final String subject, final String recipient) {
        logger.info("{} {}", normaliseSubject(subject), normaliseEmailAddress(recipient));
    }

    public void info(final MimeMessage mimeMessage) {
        info(normaliseSubject(mimeMessage), normaliseRecipients(mimeMessage));
    }

    public void info(final String subject, final Set<String> recipients) {
        info(normaliseSubject(subject), normaliseRecipients(recipients));
    }

    private static String normaliseSubject(final MimeMessage mimeMessage) {
        try {
            if (mimeMessage == null || mimeMessage.getSubject() == null) {
                return "NO_SUBJECT";
            }
            return normaliseSubject(mimeMessage.getSubject());
        } catch (MessagingException e) {
            return "NO_SUBJECT";
        }
    }

    private static String normaliseSubject(final String value) {
        return value.replaceAll("\\s+","_");
    }

    private static String normaliseRecipients(final Set<String> recipients) {
        return recipients.stream()
                .map(recipient -> normaliseEmailAddress(recipient))
                .collect(Collectors.joining(","));
    }

    private static String normaliseRecipients(final MimeMessage mimeMessage) {
        try {
            if (mimeMessage == null || ArrayUtils.isEmpty(mimeMessage.getAllRecipients())) {
                return "NO_RECIPIENTS";
            }
            return Arrays.stream(mimeMessage.getAllRecipients())
                .filter( recipient -> recipient instanceof InternetAddress)
                .map(recipient -> ((InternetAddress) recipient).getAddress())
                .collect(Collectors.joining(","));
        } catch (MessagingException e) {
            return "NO_RECIPIENTS";
        }
    }

    protected static String normaliseEmailAddress(final String emailAddress) {
        try {
            return EMAIL_PARSER.parse(emailAddress).getAddress();
        } catch (AttributeParseException e) {
            return emailAddress;
        }
    }

}
