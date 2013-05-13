package net.ripe.db.whois.query.domain;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class QueryException extends RuntimeException {
    private final Set<Message> messages;
    private final QueryCompletionInfo completionInfo;

    public QueryException(final QueryCompletionInfo completionInfo) {
        this(completionInfo, Collections.<Message>emptyList());
    }

    public QueryException(final QueryCompletionInfo completionInfo, final Message message) {
        this(completionInfo, Collections.singletonList(message));
    }

    public QueryException(final QueryCompletionInfo completionInfo, final Collection<Message> messages) {
        super(messages.size() == 1 ? messages.iterator().next().toString() : "Messages: " + messages.size());

        this.messages = Sets.newLinkedHashSet(messages);
        this.completionInfo = completionInfo;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public QueryCompletionInfo getCompletionInfo() {
        return completionInfo;
    }
}
