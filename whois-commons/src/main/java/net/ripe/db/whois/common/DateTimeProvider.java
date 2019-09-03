package net.ripe.db.whois.common;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateTimeProvider {

    /** Return the current date */
    LocalDate getCurrentDate();

    /** Return the current date time (in current system timezone) */
    LocalDateTime getCurrentDateTime();

    /** Return the current date time in UTC */
    ZonedDateTime getCurrentDateTimeUtc();

    /** returns elapsed time (with nanosecond precision). N.B. not related to system time */
    long getElapsedTime();
}
