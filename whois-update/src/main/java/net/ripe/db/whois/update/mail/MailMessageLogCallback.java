package net.ripe.db.whois.update.mail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import net.ripe.db.whois.common.conversion.PasswordFilter;
import net.ripe.db.whois.update.log.LogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MailMessageLogCallback implements LogCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailMessageLogCallback.class);

    private final Message message;

    public MailMessageLogCallback(final Message message) {
        this.message = message;
    }

    @Override
    public void log(final OutputStream outputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            message.writeTo(baos);
            String messageData = new String( baos.toByteArray(), "UTF-8" );
            String filtered = PasswordFilter.filterPasswordsInContents(messageData);
            outputStream.write(filtered.getBytes("UTF-8"));
        } catch (MessagingException e) {
            LOGGER.warn("Writing message", e);
        }
    }
}
