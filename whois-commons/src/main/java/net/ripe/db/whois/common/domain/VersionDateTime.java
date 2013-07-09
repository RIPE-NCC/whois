package net.ripe.db.whois.common.domain;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class VersionDateTime implements Comparable<VersionDateTime> {

    private final LocalDateTime timestamp;

    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public VersionDateTime(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public VersionDateTime(final Long timestamp) {
        this(new LocalDateTime(timestamp * 1000L));
    }

    public LocalDateTime toLocalDateTime() {
        return timestamp;
    }

    @Override
    public String toString() {
        return formatter.print(timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionDateTime that = (VersionDateTime) o;

        return timestamp.equals(that.timestamp);

    }

    @Override
    public int hashCode() {
        return timestamp.hashCode();
    }

    @Override
    public int compareTo(VersionDateTime other) {
        return timestamp.compareTo(other.timestamp);
    }
}
