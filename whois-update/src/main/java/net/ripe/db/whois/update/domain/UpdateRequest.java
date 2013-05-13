package net.ripe.db.whois.update.domain;

import java.util.List;

public class UpdateRequest {
    private final Origin origin;
    private final Keyword keyword;
    private final String updateMessage;
    private final List<Update> updates;
    private final boolean notificationsEnabled;

    public UpdateRequest(final Origin origin, final Keyword keyword, final String updateMessage, final List<Update> updates) {
        this(origin, keyword, updateMessage, updates, true);
    }

    public UpdateRequest(final Origin origin, final Keyword keyword, final String updateMessage, final List<Update> updates, final boolean notificationsEnabled) {
        this.origin = origin;
        this.keyword = keyword;
        this.updateMessage = updateMessage;
        this.updates = updates;
        this.notificationsEnabled = notificationsEnabled;
    }

    public Origin getOrigin() {
        return origin;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}
