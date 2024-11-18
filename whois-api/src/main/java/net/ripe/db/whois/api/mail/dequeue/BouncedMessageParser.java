package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.apache.commons.compress.utils.Lists;
import org.eclipse.angus.mail.dsn.DeliveryStatus;
import org.eclipse.angus.mail.dsn.MultipartReport;
import org.eclipse.angus.mail.dsn.Report;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BouncedMessageParser {

    private static final ContentType MULTIPART_REPORT = contentType("multipart/report");
    private static final ContentType MULTIPART_MIXED = contentType("multipart/mixed");

    private final boolean enabled;

    private static final Pattern RECIPIENT_MATCHER = Pattern.compile("(?i)^(rfc822;)\s?(.+@.+$)");

    private static final AttributeParser.EmailParser EMAIL_PARSER = new AttributeParser.EmailParser();

    @Autowired
    public BouncedMessageParser(@Value("${mail.smtp.from:}") final String smtpFrom) {
        this.enabled = !Strings.isNullOrEmpty(smtpFrom);
    }

    @Nullable
    public EmailMessageInfo parse(final MimeMessage message) throws MessagingException, MailParsingException {
        if (!enabled) {
            return null;
        }

        if (isMultipartReport(message)) {
            try {
                final MultipartReport multipartReport = multipartReport(message.getContent());
                final EmailMessageInfo recipient = extractEmailMessageInfo(message, multipartReport);
                if (recipient != null) {
                    return recipient;
                }
            } catch (MessagingException | IOException | IllegalStateException ex){
                throw new MailParsingException("Error parsing multipart report", ex);
            }
            // multipart report can *only* be a failure
            throw new MailParsingException("MultiPart message without failure report");
        }

        if (isMultipartMixed(message)) {
            try {
                final MimeMultipart multipart = multipart(message.getContent());
                final EmailMessageInfo recipient = extractEmailMessageInfo(message, multipart);
                if (recipient != null) {
                    return recipient;
                }
            } catch (MessagingException | IOException | IllegalStateException ex) {
                throw new MailParsingException("Error parsing multipart report", ex);
            }
            // do not throw an exception, as whois updates can be multipart/mixed
        }

        // fall through: message is not bounced message
        return null;
    }

    private EmailMessageInfo extractEmailMessageInfo(final MimeMessage message, final MimeMultipart multipart) throws MessagingException, IOException {
        if (isReportDeliveryStatus(multipart)) {
            final DeliveryStatus deliveryStatus = deliveryStatus(message);
            if (isFailed(deliveryStatus)) {
                final MimeMessage returnedMessage = getReturnedMessage(multipart);
                final String messageId = getMessageId(returnedMessage.getMessageID());
                final List<String> recipient = extractRecipients(deliveryStatus);
                return new EmailMessageInfo(recipient, messageId, message);
            }
        }
        return null;
    }

    private boolean isMultipartReport(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return MULTIPART_REPORT.match(contentType);
    }

    private boolean isMultipartMixed(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return MULTIPART_MIXED.match(contentType);
    }

    private boolean isReportDeliveryStatus(final MimeMultipart mimeMultipart) throws MessagingException {
        final Report report = getReport(mimeMultipart);
        return ((report != null) && "delivery-status".equals(report.getType()));
    }

    @Nullable
    private Report getReport(final MimeMultipart mimeMultipart) throws MessagingException {
        if (mimeMultipart.getCount() < 2) {
            return null;
        }
        final BodyPart bodyPart = mimeMultipart.getBodyPart(1);
        try {
            final Object content = bodyPart.getContent();
            if (!(content instanceof Report)) {
                return null;
            } else {
                return (Report) content;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    @Nullable
    private MimeMessage getReturnedMessage(final MimeMultipart mimeMultipart) throws MessagingException {
        if (mimeMultipart.getCount() < 3) {
            return null;
        }
        final BodyPart bodyPart = mimeMultipart.getBodyPart(2);
        if (!bodyPart.isMimeType("message/rfc822") &&
                !bodyPart.isMimeType("text/rfc822-headers")) {
            return null;
        } else {
            try {
                return (MimeMessage) bodyPart.getContent();
            } catch (IOException ex) {
                return null;
            }
        }
    }

    private MultipartReport multipartReport(final Object content) throws MessagingException {
        if (content instanceof MultipartReport) {
            return (MultipartReport)content;
        } else {
            throw new MessagingException("Unexpected content was not multipart/report");
        }
    }

    private MimeMultipart multipart(final Object content) throws MessagingException {
        if (content instanceof MimeMultipart) {
            return (MimeMultipart)content;
        } else {
            throw new MessagingException("Unexpected content was not multipart/mixed");
        }
    }

    private DeliveryStatus deliveryStatus(final Part part) throws MessagingException {
        try {
            return new DeliveryStatus(part.getInputStream());
        } catch (MessagingException | IOException e) {
            throw new MessagingException("Unexpected error parsing message/delivery-status part", e);
        }
    }

    private List<String> extractRecipients(final DeliveryStatus deliveryStatus) {
        final List<String> recipients = Lists.newArrayList();
        for (int dsn = 0; dsn < deliveryStatus.getRecipientDSNCount(); dsn++) {
            final String finalRecipient = parseRecipient(getHeaderValue(deliveryStatus.getRecipientDSN(dsn), "Final-Recipient"));
            if (finalRecipient != null) {
                recipients.add(finalRecipient);
            } else {
                final String originalRecipient = parseRecipient(getHeaderValue(deliveryStatus.getRecipientDSN(dsn), "Original-Recipient"));
                if (originalRecipient != null) {
                    recipients.add(originalRecipient);
                }
            }
        }
        return recipients;
    }

    @Nullable
    private String parseRecipient(final String recipient) {
        if (recipient != null) {
            final Matcher matcher = RECIPIENT_MATCHER.matcher(recipient);
            if (matcher.matches() && matcher.groupCount() == 2) {
                try {
                    return normaliseEmail(matcher.group(2));
                } catch (AttributeParseException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String normaliseEmail(final String email) {
        return EMAIL_PARSER.parse(email).getAddress();
    }

    private boolean isFailed(final DeliveryStatus deliveryStatus) {
        for (int dsn = 0; dsn < deliveryStatus.getRecipientDSNCount(); dsn++) {
            if ("failed".equals(getHeaderValue(deliveryStatus.getRecipientDSN(dsn), "Action"))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private String getHeaderValue(final InternetHeaders headers, final String name) {
        final String[] values = headers.getHeader(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    private static String getMessageId(final String messageId) {
        if (messageId == null) {
            throw new IllegalStateException("No Message-Id header");
        }
        return getAddress(messageId);
    }

    private static String getAddress(final String address) {
        if (address == null) {
            throw new IllegalStateException("No address");
        }
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
