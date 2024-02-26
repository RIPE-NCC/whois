package net.ripe.db.whois.common.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.ArrayList;
import java.util.List;

public class CustomJavaMailSender extends JavaMailSenderImpl {
    private final JavaMailSender delegate;
    private final List<EmailSendListener> listeners;

    public CustomJavaMailSender(JavaMailSender delegate) {
        this.delegate = delegate;
        this.listeners = new ArrayList<>();
    }

    public void addEmailSendListener(EmailSendListener listener) {
        listeners.add(listener);
    }


    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators){
        // Notify listeners before sending the email
        final MimeMessage mimeMessage = createMimeMessage();
        MimeMessagePreparator mimeMessagePreparator = mimeMessagePreparators[0];
        try {
            mimeMessagePreparator.prepare(mimeMessage);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Notify listeners before sending the email
        for (EmailSendListener listener : listeners) {
            listener.beforeSend(mimeMessage);
        }

        // Send the email
        delegate.send(mimeMessage);
    }

    public interface EmailSendListener {
        void beforeSend(MimeMessage message);
    }

}
