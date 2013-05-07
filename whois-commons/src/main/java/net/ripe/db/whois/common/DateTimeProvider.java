package net.ripe.db.whois.common;


import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public interface DateTimeProvider {
    LocalDate getCurrentDate();
    LocalDateTime getCurrentDateTime();
}
