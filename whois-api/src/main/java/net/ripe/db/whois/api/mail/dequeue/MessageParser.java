package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import jakarta.mail.Address;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Ref: http://www.ripe.net/data-tools/support/documentation/update-ref-manual#section-47
 */
@Component
public class MessageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParser.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
    private static final Pattern HEADER_BASE_64 = Pattern.compile("Content-Transfer-Encoding:\\s+base64*");
    private static final Session SESSION = Session.getInstance(new Properties());

    private final LoggerContext loggerContext;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public MessageParser(final LoggerContext loggerContext, final DateTimeProvider dateTimeProvider) {
        this.loggerContext = loggerContext;
        this.dateTimeProvider = dateTimeProvider;
    }

    public MailMessage parse(final String message, final UpdateContext updateContext) throws MessagingException {
        return parse(new MimeMessage(SESSION, new ByteArrayInputStream(message.getBytes())), updateContext);
    }

    public MailMessage parse(final InputStream message, final UpdateContext updateContext) throws MessagingException {
        return parse(new MimeMessage(SESSION, message), updateContext);
    }

    public MailMessage parse(final MimeMessage message, final UpdateContext updateContext) throws MessagingException {
        final MailMessageBuilder messageBuilder = new MailMessageBuilder();
        messageBuilder.id(message.getMessageID());

        parseSubject(messageBuilder, message, updateContext);

        String[] deliveryDate = message.getHeader("Delivery-date");
        if (deliveryDate != null && deliveryDate.length > 0 && deliveryDate[0].length() > 0) {
            ZonedDateTime deliveryDateInUTC = ZonedDateTime
                    .parse(deliveryDate[0], RFC_1123_DATE_TIME).withZoneSameInstant(ZoneOffset.UTC);

            messageBuilder.date(DATE_FORMAT.format(deliveryDateInUTC));
        } else {
            messageBuilder.date(DATE_FORMAT.format(dateTimeProvider.getCurrentZonedDateTime()));
        }

        parseReplyTo(messageBuilder, message);

        try {
            parseContents(messageBuilder, message);

            final MailMessage mailMessage = messageBuilder.build();
            if (!mailMessage.getKeyword().isContentExpected() || !mailMessage.getContentWithCredentials().isEmpty()) {
                return mailMessage;
            }
        } catch (ParseException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to parse message"), e);
        } catch (MessagingException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Unable to parse message using java mail"), e);
        } catch (IOException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Exception parsing message"), e);
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error parsing message: {}", message.getMessageID(), e);
        }

        updateContext.addGlobalMessage(UpdateMessages.noValidUpdateFound());
        return messageBuilder.build();
    }

    private void parseSubject(final MailMessageBuilder messageBuilder, final MimeMessage message, final UpdateContext updateContext) throws MessagingException {
        String subject = message.getSubject();
        messageBuilder.subject(subject);

        if (StringUtils.isNotBlank(subject)) {
            final Keyword keyword = Keyword.getByKeyword(subject.trim());
            if (keyword == null) {
                updateContext.addGlobalMessage(UpdateMessages.invalidKeywordsFound(subject));
                updateContext.addGlobalMessage(UpdateMessages.allKeywordsIgnored());
            } else {
                if (keyword.equals(Keyword.DIFF)) {
                    updateContext.addGlobalMessage(UpdateMessages.diffNotSupported());
                }
                messageBuilder.keyword(keyword);
            }
        }
    }

    private void parseReplyTo(@Nonnull final MailMessageBuilder messageBuilder, @Nonnull final MimeMessage message) throws MessagingException {
        try {
            Address[] replyTo = message.getReplyTo();
            if (replyTo != null && replyTo.length > 0) {
                messageBuilder.replyTo(replyTo[0].toString());
                messageBuilder.replyToEmail(((InternetAddress) replyTo[0]).getAddress());
            }

            Address[] from = message.getFrom();
            if (from != null && from.length > 0) {
                messageBuilder.from(from[0].toString());
                if (StringUtils.isBlank(messageBuilder.getReplyTo())) {
                    messageBuilder.replyTo(from[0].toString());
                    messageBuilder.replyToEmail(((InternetAddress) from[0]).getAddress());
                }
            }
        } catch (AddressException e) {
            loggerContext.log(new Message(Messages.Type.ERROR, "Could not parse from/reply-to header"), e);
        }
    }

    private void parseContents(@Nonnull final MailMessageBuilder messageBuilder, @Nonnull final MimeMessage message) throws MessagingException, IOException {
        final MessageParts messageParts = new MessageParts();

        parseContents(messageParts, message, null);

        for (final MessagePart messagePart : messageParts.parts) {
            final List<Credential> credentials = messagePart.credentials;
            if (credentials.size() > 1) {
                throw new ParseException("Multiple credentials for text content");
            }

            final Charset charset = getCharset(new ContentType(message.getContentType()));

            messageBuilder.addContentWithCredentials(new ContentWithCredentials(messagePart.text, credentials, charset));
        }
    }

    private void parseContents(@Nonnull final MessageParts messageParts, @Nonnull final Part part, @Nullable final Part parentPart) throws MessagingException, IOException {
        handleInvalidTypes(part);
        final ContentType contentType = new ContentType(part.getContentType());
        final Object content = getContent(part, contentType);
        final Charset charset = getCharset(contentType);

        if (isPlainText(contentType)) {
            final String text;
            if (content instanceof String) {
                text = (String) content;
            } else if (content instanceof InputStream) {
                text = new String(ByteStreams.toByteArray((InputStream) content), charset);
            } else {
                throw new ParseException("Unexpected content: " + content);
            }

            messageParts.add(new MessagePart(text, getSignedPart(part, parentPart)));
        } else if (content instanceof MimeMultipart) {
            final MimeMultipart multipart = (MimeMultipart) content;
            for (int count = 0; count < multipart.getCount(); count++) {
                final Part bodyPart = multipart.getBodyPart(count);
                final ContentType bodyPartContentType = new ContentType(bodyPart.getContentType());

                if (bodyPartContentType.getBaseType().equals("multipart/mixed") && contentType.getBaseType().equals("multipart/signed")) {
                    // nested multipart signed message
                    messageParts.add(new MessagePart(getHeaders(bodyPart) + getRawContent(bodyPart, bodyPartContentType.getParameter("boundary")), bodyPart));
                } else if (bodyPartContentType.getBaseType().equals("application/pgp-signature")) {
                    final MessagePart last = messageParts.getLast();
                    final String signedData = getHeaders(last.part) + getRawContent(last.part);

                    if (isBase64(bodyPart)) {
                        final String signature = getContent(bodyPart);
                        last.addCredential(PgpCredential.createOfferedCredential(signedData, signature, charset));
                    } else {
                        final String signature = getRawContent(bodyPart);
                        last.addCredential(PgpCredential.createOfferedCredential(signedData, signature, charset));
                    }
                } else if (bodyPartContentType.getBaseType().equals("application/pkcs7-signature") ||
                            bodyPartContentType.getBaseType().equals("application/x-pkcs7-signature")) {
                    final MessagePart last = messageParts.getLast();
                    final String signedData = (getHeaders(last.part).replaceAll("\\r\\n", "\n") + getRawContent(last.part)).replaceAll("\\n", "\r\n");
                    final String signature = getRawContent(bodyPart);
                    last.addCredential(X509Credential.createOfferedCredential(signedData, signature));
                } else {
                    parseContents(messageParts, bodyPart, part);
                }
            }
        }
    }

    private static void handleInvalidTypes(Part part) throws MessagingException {
        if ("text".equals(part.getContentType())){
            part.setHeader("Content-Type", "text/plain;");
        }
    }

    private Part getSignedPart(final Part part, final Part parentPart) throws MessagingException {
        if (parentPart != null && new ContentType(parentPart.getContentType()).getBaseType().equals("multipart/alternative")) {
            return parentPart;
        }

        return part;
    }

    private Object getContent(final Part part, final ContentType contentType) throws MessagingException, IOException {
        try {
            return part.getContent();
        } catch (IOException e) {
            if (isPlainText(contentType)) {
                final InputStream rawInputStream;
                if (part instanceof MimeMessage) {
                    rawInputStream = ((MimeMessage) part).getRawInputStream();
                } else if (part instanceof MimeBodyPart) {
                    rawInputStream = ((MimeBodyPart) part).getRawInputStream();
                } else {
                    throw new ParseException("Unexpected part: " + part);
                }

                return new String(ByteStreams.toByteArray(rawInputStream), getCharset(contentType));
            }
        }

        throw new ParseException("No content");
    }

    private boolean isPlainText(final ContentType contentType) {
        return contentType.getBaseType().toLowerCase().equals("text/plain") ||
                contentType.getBaseType().toLowerCase().equals("application/pgp");
    }

    Charset getCharset(final ContentType contentType) {
        final String charset = contentType.getParameter("charset");
        if (charset != null) {
            try {
                return Charset.forName(MimeUtility.javaCharset(charset));
            } catch (UnsupportedCharsetException e) {
                loggerContext.log(new Message(Messages.Type.WARNING, "Unsupported charset: %s in contentType: %s", charset, contentType));
            } catch (IllegalCharsetNameException e) {
                loggerContext.log(new Message(Messages.Type.WARNING, "Illegal charset: %s in contentType: %s", charset, contentType));
            }
        }

        return StandardCharsets.ISO_8859_1;
    }

    String getHeaders(final Part part) throws MessagingException {
        StringBuilder builder = new StringBuilder();
        @SuppressWarnings("unchecked")
        final Enumeration<Header> headers = part.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header next = headers.nextElement();
            builder.append(next.getName());
            builder.append(": ");
            builder.append(next.getValue());
            builder.append('\n');
            if (!headers.hasMoreElements()) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    //TODO: Refactor the code properly to avoid manually decoding base64.
    // part.getContents() does gives us plain text but the PGP is signed including the boundary and headers from parent part.
    String getRawContent(final Part part, final String boundary) throws MessagingException {
        final String rawContent = getRawContent(part);
        return getDecodedContent(rawContent, boundary);
    }

    String getRawContent(final Part part) throws MessagingException {
        try (final InputStream inputStream = ((MimeBodyPart) part).getRawInputStream()) {
            return new String(ByteStreams.toByteArray(inputStream), getCharset(new ContentType(part.getContentType())));
        } catch (IOException e) {
            throw new MessagingException("Unable to read body part", e);
        }
    }

    private String getDecodedContent(final String rawContent, final String boundary) {
        final Matcher matcher = HEADER_BASE_64.matcher(rawContent);

        if(boundary == null || !matcher.find()) {
            return rawContent;
        }

        //Boundary parameter in contentType exactly has two extra "--" as start and end of boundary
        final String encodedContent = StringUtils.substringBetween(rawContent, matcher.group(0), "--".concat(boundary)).trim();
        final String decodedContent = decodeBase64Encoding(encodedContent);

        return rawContent.replace(encodedContent, decodedContent);
    }

    String getContent(final Part part) throws MessagingException {
        try (InputStream inputStream = part.getInputStream()) {
            return new String(ByteStreams.toByteArray(inputStream), getCharset(new ContentType(part.getContentType())));
        } catch (IOException e) {
            throw new MessagingException("Unable to read body part", e);
        }
    }

    boolean isBase64(final Part part) throws MessagingException {
        final String[] headers = part.getHeader("Content-Transfer-Encoding");
        return ((headers != null) &&
                (headers.length > 0) &&
                ("base64".equals(headers[0])));
    }

    private static class MessageParts {
        private final List<MessagePart> parts = Lists.newArrayList();

        void add(final MessagePart part) {
            parts.add(part);
        }

        MessagePart getLast() {
            if (parts.isEmpty()) {
                throw new ParseException("No message part yet");
            }

            return parts.get(parts.size() - 1);
        }
    }

    private static final class MessagePart {
        private final String text;
        private final Part part;
        private final List<Credential> credentials = Lists.newArrayList();

        private MessagePart(final String text, final Part part) {
            this.text = text;
            this.part = part;
        }

        void addCredential(final Credential credential) {
            credentials.add(credential);
        }
    }

    private String decodeBase64Encoding(final String paragraph) {
        try {
            return new String(new Base64().decode(paragraph.getBytes()));
        } catch (Exception e) {
            LOGGER.info("failed to decode base64 due to {}: {}", e.getClass().getName(), e.getMessage());
            return paragraph;
        }
    }
}
