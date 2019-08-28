package net.ripe.db.whois.common.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Abstraction for database timestamp
 */
public class Timestamp {

    // database timestamp value (in seconds since unix epoch)
    private final long value;

    /**
     * Create a new timestamp
     * @param value database timestamp (with second precision)
     */
    public Timestamp(final long value) {
        this.value = value;
    }

    public Timestamp(final LocalDateTime localDateTime) {
        this.value = toSeconds(Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli());
    }

    /**
     * @return database timestamp as a long (with second precision)
     */
    public long getValue() {
        return value;
    }

    /**
     * Convert from database timestamp to Java Time object.
     * @return
     */
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.from(Instant.ofEpochMilli(toMilliseconds(value)).atZone(ZoneOffset.systemDefault()));
    }

    /**
     * Convert from database timestamp (with seconds precision) to java time (with milliseconds precision).
     * @return
     */
    private long toMilliseconds(final long value) {
        return value * 1000L;
    }

    /**
     * Convert from java time (with milliseconds precision) to database timestamp (with seconds precision)
     * @return
     */
    private long toSeconds(final long value) {
        return value / 1000L;
    }
}
