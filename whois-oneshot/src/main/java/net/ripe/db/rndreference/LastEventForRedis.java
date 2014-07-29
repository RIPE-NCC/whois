package net.ripe.db.rndreference;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class LastEventForRedis {
    private Long timestamp;
    private String object;
    private boolean deleteEvent;

    LastEventForRedis(final Long timestamp, final RpslObject object, boolean deleteEvent) {
        this.timestamp = timestamp;
        if (object != null) {
            this.object = object.toString();
        }
        this.deleteEvent = deleteEvent;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getObject() {
        return object;
    }

    public boolean getDeleteEvent() {
        return deleteEvent;
    }

    public void setDeleteEvent(final boolean deleteEvent) {
        this.deleteEvent = deleteEvent;
    }
}

