package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;

// Detect automated responses and mark them for deletion. Do not try to parse them as Whois updates (for now).
// TODO: attempt to find the outgoing message-id and failed recipient if possible by parsing the plaintext body
@Component
public class AutoSubmittedMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSubmittedMessageParser.class);

    private final boolean enabled;

    @Autowired
    public AutoSubmittedMessageParser(@Value("${mail.smtp.from:}") final String smtpFrom) {
        this.enabled = !StringUtils.isEmpty(smtpFrom);
    }

    @Nullable
    public EmailMessageInfo parse(final MimeMessage message) throws MessagingException, MailParsingException {
        if (!enabled) {
            return null;
        }

        final String autoSubmitted = getHeader(message, "Auto-Submitted");
        if (autoSubmitted != null) {
            if (autoSubmitted.contains("auto-generated") || autoSubmitted.contains("auto-replied")) {
                return new EmailMessageInfo(Collections.emptyList(), null, null);
            } else {
                LOGGER.info("Unexpected Auto-Submitted value {}", autoSubmitted);
            }
        }

        final String from = getHeader(message, "From");
        if (from != null) {
            if (from.toUpperCase().contains("MAILER-DAEMON")) {
                return new EmailMessageInfo(Collections.emptyList(), null, null);
            }
        }

        return null;
    }

    @Nullable
    private String getHeader(final MimeMessage message, final String name) throws MessagingException {
        final String[] headers = message.getHeader(name);
        if (headers != null && headers.length > 0) {
            return headers[0];
        }
        return null;
    }
}
