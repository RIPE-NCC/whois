package net.ripe.db.whois.api.mail;

import javax.annotation.Nullable;

public class BouncedMessage {

    private final String emailAddress;
    private final String messageId;

    public BouncedMessage(final String emailAddress, final String messageId) {
        this.emailAddress = emailAddress;
        this.messageId = messageId;
    }

    @Nullable
    public String getMessageId() {
        return messageId;
    }

    @Nullable
    public String getEmailAddress() {
        return emailAddress;
    }
}
