package net.ripe.db.whois.common;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public interface DateTimeProvider {
    LocalDate getCurrentDate();
    LocalDateTime getCurrentDateTime();
    ZonedDateTime getCurrentDateTimeUtc();

    /** returns elapsed time (with nanosecond precision). N.B. not related to system time */
    long getElapsedTime();

    /** Convert from Java timestamp (with millisecond precision) into Java time object.
     * Specify the local timezone (not UTC).
     */
    static LocalDateTime fromEpochMilli(final long timestamp) {
        return LocalDateTime.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.systemDefault()));
    }

    /** Convert time to timestamp (with millsecond precision) */
    static long toEpochMilli(final LocalDateTime localDateTime) {
        return Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli();
    }

    /** Convert from Java date time to a Date object.
     * Specify the local timezone (not UTC).
     */
    static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.systemDefault())
                    .toInstant());
    }

    /** Convert from Java date to a Date object.
     * Specify the local timezone (not UTC).
     */
    static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
    }

    /** Convert from a Date to a Java date time object.
     * Specify the local timezone (not UTC).
     */
    static LocalDateTime fromDate(final Date date) {
        return Instant.ofEpochMilli(date.getTime())
                        .atZone(ZoneOffset.systemDefault())
                        .toLocalDateTime();
    }

}
