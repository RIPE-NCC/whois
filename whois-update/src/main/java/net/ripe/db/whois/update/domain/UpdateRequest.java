package net.ripe.db.whois.update.domain;

import java.util.Collection;

public class UpdateRequest {
    private final Origin origin;
    private final Keyword keyword;
    private final Collection<Update> updates;

    public UpdateRequest(final Origin origin, final Keyword keyword, final Collection<Update> updates) {
        this.origin = origin;
        this.keyword = keyword;
        this.updates = updates;
    }

    public Origin getOrigin() {
        return origin;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public Collection<Update> getUpdates() {
        return updates;
    }
}
