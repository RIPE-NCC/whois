package net.ripe.db.whois.query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class VersionDateTime implements Comparable<VersionDateTime> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalDateTime timestamp;


    public VersionDateTime(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public VersionDateTime(final Long timestamp) {
        this(fromEpochMilli(timestamp * 1000L));
    }

    // TODO: [ES] copied from DateTimeProvider
    private static LocalDateTime fromEpochMilli(final long timestamp) {
        return LocalDateTime.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.systemDefault()));
    }

    @Override
    public String toString() {
        return DATE_TIME_FORMATTER.format(timestamp);
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
