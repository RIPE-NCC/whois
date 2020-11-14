package net.ripe.db.whois.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Convert to/from java.util.Date instances
 */
public final class DateUtil {

    private DateUtil() {
        // do not instantiate
    }

    /** Convert from Java date time to a Date object.
     * Specify the local timezone (not UTC).
     */
    public static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.systemDefault())
                    .toInstant());
    }

    /** Convert from Java date to a Date object.
     * Specify the local timezone (not UTC).
     */
    public static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
    }

    /** Convert from a Date to a Java date time object.
     * Specify the local timezone (not UTC).
     */
    public static LocalDateTime fromDate(final Date date) {
        return Instant.ofEpochMilli(date.getTime())
                        .atZone(ZoneOffset.systemDefault())
                        .toLocalDateTime();
    }
}



