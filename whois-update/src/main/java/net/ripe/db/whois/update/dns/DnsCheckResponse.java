package net.ripe.db.whois.update.dns;

import net.ripe.db.whois.common.Message;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public class DnsCheckResponse {
    private final List<Message> messages;

    public DnsCheckResponse() {
        this.messages = Collections.emptyList();
    }

    public DnsCheckResponse(Message messages) {
        this.messages = Collections.singletonList(messages);
    }

    public DnsCheckResponse(final List<Message> messages) {
        this.messages = Collections.unmodifiableList(messages);
    }

    public List<Message> getMessages() {
        return messages;
    }
}
