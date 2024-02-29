package net.ripe.db.whois.api;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

@Component
public class MailUpdatesTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailUpdatesTestSupport.class);

    private static final Session SESSION = Session.getInstance(new Properties());

    private final MailMessageDao mailMessageDao;

    @Autowired
    public MailUpdatesTestSupport(final MailMessageDao mailMessageDao) {
        this.mailMessageDao = mailMessageDao;
    }

    public String insert(final String content) {
        try {
            final InputStream is = new ByteArrayInputStream(content.getBytes());
            final MimeMessage message = new MimeMessage(SESSION, is);
            mailMessageDao.addMessage(message);
            final Address[] from = message.getFrom();
            return (from.length > 0) ? ((InternetAddress)from[0]).getAddress() : null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to send mail", e);
        }
    }

    public void insert(final MimeMessage message) {
        addMessage(message);
    }

    public String insert(final String subject, final String body) {
        try {
            final String from = UUID.randomUUID() + "@ripe.net";
            addMessage(from, "rdp-dev@ripe.net", subject, body);
            return from;
        } catch (Exception e) {
            throw new RuntimeException("Unable to send mail", e);
        }
    }


    private void addMessage(final String from, final String to, final String subject, final String body) throws MessagingException {
        LOGGER.info("Send email from address {} with subject {}", from, subject);

        final MimeMessage message = new MimeMessage(SESSION);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        addMessage(message);
    }

    private void addMessage(final MimeMessage message) {
        mailMessageDao.addMessage(message);
    }
}
