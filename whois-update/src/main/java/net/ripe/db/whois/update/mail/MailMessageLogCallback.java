package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.log.LogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
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
        try {
            message.writeTo(outputStream);
        } catch (MessagingException e) {
            LOGGER.warn("Writing message", e);
        }
    }
}
