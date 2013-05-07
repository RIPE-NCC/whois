package net.ripe.db.whois.update.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

public abstract class MailSenderBase implements JavaMailSender {
    @Override
    public MimeMessage createMimeMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(MimeMessage[] mimeMessages) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(SimpleMailMessage[] simpleMessages) throws MailException {
        throw new UnsupportedOperationException();
    }
}
