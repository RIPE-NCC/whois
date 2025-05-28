package net.ripe.db.whois.common;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateTimeProvider {

    /** Return the current date in UTC */
    LocalDate getCurrentDate();

    /** Return the current date time in UTC */
    LocalDateTime getCurrentDateTime();

    /** Return the current date time include zone information in UTC  */
    ZonedDateTime getCurrentZonedDateTime();

    /** returns elapsed time (with nanosecond precision). N.B. not related to system time */
    long getElapsedTime();
}
