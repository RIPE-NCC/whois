package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.BouncedMessage;
import org.eclipse.angus.mail.dsn.DeliveryStatus;
import org.eclipse.angus.mail.dsn.MultipartReport;
import org.eclipse.angus.mail.dsn.Report;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

@Component
public class BouncedMessageParser {

    private static final Session SESSION = Session.getInstance(new Properties());

    private static final ContentType MULTIPART_REPORT = contentType("multipart/report");
    private static final ContentType MESSAGE_RFC822 = contentType("message/rfc822");
    private static final ContentType RFC822_HEADERS = contentType("text/rfc822-headers");

    @Nullable
    public BouncedMessage parse(final MimeMessage message) throws MessagingException, IOException {
        if (isMultipartReport(message)) {
            final MultipartReport multipartReport = multipartReport(message.getContent());
            if (isReportDeliveryStatus(multipartReport)) {
                final DeliveryStatus deliveryStatus = deliveryStatus(message);
                if (isFailed(deliveryStatus)) {
                    for (int i=0; i < multipartReport.getCount(); i++) {
                        final BodyPart part = multipartReport.getBodyPart(i);
                        final ContentType partContentType = contentType(part.getContentType());
                        if ((MESSAGE_RFC822.match(partContentType) || RFC822_HEADERS.match(partContentType))) {
                            final MimeMessage partBody = new MimeMessage(SESSION, part.getInputStream());
                            final String messageId = getAddress(getHeaderValue(partBody.getAllHeaders(), "Message-Id"));
                            final String to = getAddress(getHeaderValue(partBody.getAllHeaders(), "To"));
                            return new BouncedMessage(to, messageId);
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isMultipartReport(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return MULTIPART_REPORT.match(contentType);
    }

    private boolean isReportDeliveryStatus(final MultipartReport multipartReport) throws MessagingException {
        final Report report = multipartReport.getReport();
        return ("delivery-status".equals(report.getType()));
    }

    private MultipartReport multipartReport(final Object content) throws MessagingException {
        if (content instanceof MultipartReport) {
            return (MultipartReport)content;
        } else {
            throw new MessagingException("Unexpected content was not multipart/report");
        }
    }

    private DeliveryStatus deliveryStatus(final Part part) throws MessagingException {
        try {
            return new DeliveryStatus(part.getInputStream());
        } catch (MessagingException | IOException e) {
            throw new MessagingException("Unexpected error parsing message/delivery-status part", e);
        }
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

    private String getHeaderValue(final Enumeration<Header> headers, final String name) {
        while (headers.hasMoreElements()) {
            final Header header = headers.nextElement();
            if (name.equals(header.getName())) {
                return header.getValue();
            }
        }
        throw new IllegalStateException("No header " + name);
    }

    private final String getAddress(final String address) {
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
