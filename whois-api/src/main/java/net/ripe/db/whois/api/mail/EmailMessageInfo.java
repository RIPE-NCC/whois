package net.ripe.db.whois.api.mail;

import javax.annotation.Nullable;
import java.util.List;

public record EmailMessageInfo(List<String> emailAddresses, String messageId) {

    @Override
    @Nullable
    public String messageId() {
        return messageId;
    }

    @Override
    @Nullable
    public List<String> emailAddresses() {
        return emailAddresses;
    }
}
