package net.ripe.db.whois.update.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Profile({WhoisProfile.DEPLOYED})
@Component
public class CustomJavaMailSender extends JavaMailSenderImpl {

    @Override
    public MimeMessage createMimeMessage() {
        return new CustomMimeMessage(getSession());
    }

    static class CustomMimeMessage extends MimeMessage{

        private final String messageId;

        public CustomMimeMessage(final Session session) {
            super(session);
            this.messageId = String.format("<%s@ripe.net>", UUID.randomUUID());
        }

        @Override
        protected void updateMessageID() throws MessagingException {
            setHeader("Message-ID", messageId);
        }

        @Override
        public String getMessageID() {
            return messageId;
        }
    }
}


