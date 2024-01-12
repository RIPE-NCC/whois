package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@DeployedProfile
@Primary
@Component
public class ClockDateTimeProvider implements DateTimeProvider {

    @Override
    public LocalDate getCurrentDate() {
        return getCurrentZonedDateTime().toLocalDate();
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return getCurrentZonedDateTime().toLocalDateTime();
    }

    @Override
    public ZonedDateTime getCurrentZonedDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    /** Returns System.nanoTime(), the high-res timer that counts 0 from JVM startup.
     *  N.B. "This method can only be used to measure elapsed time and is not related to any other notion of system or wall-clock time."
     */
    @Override
    public long getElapsedTime() {
        return System.nanoTime();
    }
}
