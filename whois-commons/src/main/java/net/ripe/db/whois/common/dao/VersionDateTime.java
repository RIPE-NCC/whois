package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.Timestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class VersionDateTime implements Comparable<VersionDateTime> {

    private final LocalDateTime timestamp;


    public VersionDateTime(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public VersionDateTime(final long timestamp) {
        this((Timestamp.fromSeconds(timestamp)).toLocalDateTime());
    }

    @Override
    public String toString() {
        return FormatHelper.dateTimeToUtcString(timestamp);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VersionDateTime that = (VersionDateTime) o;

        return Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }

    @Override
    public int compareTo(final VersionDateTime other) {
        return timestamp.compareTo(other.timestamp);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
