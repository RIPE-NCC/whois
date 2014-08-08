package net.ripe.db.rndreference;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;

public class DatabaseRpslObject implements Comparable<DatabaseRpslObject> {
    private DateTime eventDate;

    public DatabaseRpslObject(final DateTime eventDate) {
        this.eventDate = eventDate;
    }

    public DateTime getEventDate() {
        return eventDate;
    }

    @Override
    public int compareTo(@Nonnull final DatabaseRpslObject o) {
        return this.eventDate.compareTo(o.getEventDate());
    }

}
