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

    private Timestamp(final long value) {
        this.value = value;
    }

    /**
     * Create a new timestamp
     * @param localDateTime java time (with milliseconds precision)
     */
    public static Timestamp from(final LocalDateTime localDateTime) {
        return new Timestamp(toSeconds(Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli()));
    }

    /**
     * Create a new timestamp
     * @param timestamp sql timestamp (with milliseconds precision)
     */
    public static Timestamp from(final java.sql.Timestamp timestamp) {
        return new Timestamp(toSeconds(timestamp.getTime()));
    }

    /**
     * Create a new timestamp
     * @param text text string containing valid local date-time
     * @return
     */
    public static Timestamp from(final CharSequence text) {
        return new Timestamp(LocalDateTime.parse(text).atZone(ZoneOffset.systemDefault()).toEpochSecond());
    }

    /**
     * Create a new timestamp
     * @param value timestamp value in seconds since unix epoch
     * @return
     */
    public static Timestamp fromSeconds(final long value) {
        return new Timestamp(value);
    }

    /**
     * Create a new timestamp
     * @param value timestamp value in milliseconds since unix epoch
     * @return
     */
    public static Timestamp fromMilliseconds(final long value) {
        return new Timestamp(toSeconds(value));
    }

    /**
     * @return database timestamp (in seconds since unix epoch)
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
     * Convert value with second precision to millisecond precision
     * @return
     */
    private static long toMilliseconds(final long value) {
        return value * 1000L;
    }

    /**
     * Convert value with millisecond precision to seconds precision
     * @return
     */
    private static long toSeconds(final long value) {
        return value / 1000L;
    }
}
