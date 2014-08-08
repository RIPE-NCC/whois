package net.ripe.db.rndreference;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.DateTime;

import javax.annotation.CheckForNull;

public class LastEvent extends DatabaseRpslObject {
    private final boolean deleteEvent;
    private final RpslObject rpslObject;

    public LastEvent(@CheckForNull final RpslObject rpslObject, final DateTime eventDate, final boolean deleteEvent) {
        super(eventDate);
        this.deleteEvent = deleteEvent;
        this.rpslObject = rpslObject;
    }

    public boolean isDeleteEvent() {
        return deleteEvent;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }
}
