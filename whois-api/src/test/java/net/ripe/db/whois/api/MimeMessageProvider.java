package net.ripe.db.whois.api;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Properties;

/**
 * @deprecated Do not use for new tests, mail messages contain customer data
 */
@Deprecated
public final class MimeMessageProvider {

    private static final Session SESSION = Session.getInstance(new Properties());

    public static MimeMessage getMessageMultipartPgpSigned() {
        return getUpdateMessage("multipartPgpSigned.mail");
    }

    public static MimeMessage getMessageSimpleTextUnsigned() {
        return getUpdateMessage("simplePlainTextUnsigned.mail");
    }

    public static MimeMessage getMessageMultipartAlternativePgpSigned() {
        return getUpdateMessage("multipartAlternativePgpSigned.mail");
    }

    public static MimeMessage getUpdateMessage(final String filename) {
        return getUpdateMessage(new ClassPathResource("testMail/" + filename));
    }

    private static MimeMessage getUpdateMessage(final Resource resource) {
        try {
            final InputStream inputStream = resource.getInputStream();
            try {
                return new MimeMessage(SESSION, inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail", e);
        }
    }
}
