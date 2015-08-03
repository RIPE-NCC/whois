package net.ripe.db.whois.common;


import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public interface DateTimeProvider {
    LocalDate getCurrentDate();
    LocalDateTime getCurrentDateTime();
    DateTime getCurrentDateTimeUtc();

    /** returns System.nanoTime(), the high-res timer that counts 0 from JVM startup */
    long getNanoTime();
}
