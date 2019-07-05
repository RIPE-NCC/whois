package net.ripe.db.whois.common;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public interface DateTimeProvider {
    LocalDate getCurrentDate();
    LocalDateTime getCurrentDateTime();
    ZonedDateTime getCurrentDateTimeUtc();

    /** returns System.nanoTime(), the high-res timer that counts 0 from JVM startup */
    long getNanoTime();

    static LocalDateTime fromEpochMilli(final long timestamp) {
        return LocalDateTime.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.systemDefault()));
    }

    static long toEpochMilli(final LocalDateTime localDateTime) {
        return Instant.from(localDateTime.atZone(ZoneOffset.systemDefault())).toEpochMilli();
    }

    static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault())
                    .toInstant());
    }

    static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    static LocalDateTime fromDate(final Date date) {
        return Instant.ofEpochMilli(date.getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
    }

}
