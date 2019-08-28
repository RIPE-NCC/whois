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
     * @param value database timestamp (e.g. from INT(10) column) (with second precision)
     */
    public Timestamp(final long value) {
        this.value = value;
    }

    /**
     * Create a new timestamp
     * @param localDateTime java time (with milliseconds precision)
     */
    public Timestamp(final LocalDateTime localDateTime) {
        this.value = toSeconds(Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli());
    }

    /**
     * Create a new timestamp
     * @param timestamp sql timestamp (with milliseconds precision)
     */
    public Timestamp(final java.sql.Timestamp timestamp) {
        this.value = toSeconds(timestamp.getTime());
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
    private static long toMilliseconds(final long value) {
        return value * 1000L;
    }

    /**
     * Convert from java time (with milliseconds precision) to database timestamp (with seconds precision)
     * @return
     */
    private static long toSeconds(final long value) {
        return value / 1000L;
    }
}
