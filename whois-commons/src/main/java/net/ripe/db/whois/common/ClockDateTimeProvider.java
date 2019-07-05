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
        return LocalDate.now();
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public ZonedDateTime getCurrentDateTimeUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public long getNanoTime() {
        return System.nanoTime();
    }
}
