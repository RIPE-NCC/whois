package net.ripe.db.whois.api.mail;

import javax.annotation.Nullable;

public record MessageInfo(String emailAddress, String messageId) {

    @Override
    @Nullable
    public String messageId() {
        return messageId;
    }

    @Override
    @Nullable
    public String emailAddress() {
        return emailAddress;
    }
}
