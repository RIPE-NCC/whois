package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import net.ripe.db.whois.api.mail.BouncedMessage;
import org.eclipse.angus.mail.dsn.DeliveryStatus;
import org.eclipse.angus.mail.dsn.MultipartReport;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;

@Component
public class BouncedMessageParser {

    private static final ContentType MULTIPART_REPORT = contentType("multipart/report");

    private static final String DELIVERY_STATUS = "delivery-status";
    private final boolean enabled;

    @Autowired
    public BouncedMessageParser(@Value("${mail.smtp.from:}") final String smtpFrom) {
        this.enabled = Strings.hasLength(smtpFrom);
    }

    @Nullable
    public BouncedMessage parse(final MimeMessage message) throws MessagingException, IOException {
        if (enabled && isMultipartReport(message)) {
            final MimeReportInfo mimeReportInfo = mimeReport(message.getContent());
            if (mimeReportInfo.isReportDeliveryStatus()) {
                final DeliveryStatus deliveryStatus = deliveryStatus(message);
                if (isFailed(deliveryStatus)) {
                    final MimeMessage returnedMessage = mimeReportInfo.getReportReturningMessage();
                    final String messageId = getMessageId(returnedMessage.getMessageID());
                    // TODO: double check we have the right recipient (This is the TO: header)
                    final String recipient = getFirstAddress(returnedMessage.getAllRecipients());
                    return new BouncedMessage(recipient, messageId);
                }
            }
        }

        return null;
    }

    @Nullable
    private String getFirstAddress(final Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            throw new IllegalStateException("No address");
        }
        return addresses[0].toString();
    }

    private boolean isMultipartReport(final MimeMessage message) throws MessagingException {
        final ContentType contentType = contentType(message.getContentType());
        return MULTIPART_REPORT.match(contentType);
    }

    private MimeReportInfo mimeReport(final Object content) throws MessagingException, IOException {
        if (content instanceof final MultipartReport multipartReport) {
            return new MimeReportInfo(DELIVERY_STATUS.equalsIgnoreCase(multipartReport.getReport().getType()), multipartReport.getReturnedMessage());
        }
        if (content instanceof final MimeMultipart mimeMultipart) {
            return getReportMessageInformation(mimeMultipart);
        }

        throw new MessagingException("Unexpected content was not multipart/report");

    }

    private static MimeReportInfo getReportMessageInformation(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        final MimeReportInfo mimeReportInfo = new MimeReportInfo();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            final MimeBodyPart bodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(i);
            final ContentType contentType = new ContentType(bodyPart.getContentType());
            if (DELIVERY_STATUS.equalsIgnoreCase(contentType.getSubType())) {
                mimeReportInfo.setMimeReportType(true);
            }
            if ("rfc822".equalsIgnoreCase(contentType.getSubType())){
                mimeReportInfo.setReportReturningMessage(new MimeMessage(null, bodyPart.getInputStream()));
            }
        }
        return mimeReportInfo;
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
