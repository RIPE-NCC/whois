package net.ripe.db.whois.update.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.util.UUID;

public class CustomMimeMessage extends MimeMessage {
    private String messageId;
    public CustomMimeMessage(Session session){
        super(session);
    }


    @Override
    protected void updateMessageID() throws MessagingException {
        messageId = String.format("<%s@ripe.net>", UUID.randomUUID());
        setHeader("Message-ID", messageId);
    }

    @Override
    public String getMessageID() {
        return messageId;
    }
}
