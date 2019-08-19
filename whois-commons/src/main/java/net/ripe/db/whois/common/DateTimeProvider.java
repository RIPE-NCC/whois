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

    /** returns elapsed time. N.B. not related to system time */
    long getElapsedTime();

    /** specify the local timezone when creating a LocalDateTime from a timestamp */
    static LocalDateTime fromEpochMilli(final long timestamp) {
        return LocalDateTime.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.systemDefault()));
    }

    static long toEpochMilli(final LocalDateTime localDateTime) {
        return Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli();
    }

    static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.systemDefault())
                    .toInstant());
    }

    static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
    }

    static LocalDateTime fromDate(final Date date) {
        return Instant.ofEpochMilli(date.getTime())
                        .atZone(ZoneOffset.systemDefault())
                        .toLocalDateTime();
    }

}
