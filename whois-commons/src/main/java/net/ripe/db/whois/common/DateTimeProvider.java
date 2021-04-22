package net.ripe.db.whois.common;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateTimeProvider {

    LocalDate getLocalDateUtc();

    LocalDateTime getLocalDateTimeUtc();

    ZonedDateTime getZoneDateTimeUtc();

    /** returns elapsed time (with nanosecond precision). N.B. not related to system time */
    long getElapsedTime();
}
