package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@DeployedProfile
@Primary
@Component
public class ClockDateTimeProvider implements DateTimeProvider {
    @Override
    public LocalDate getCurrentDate() {
        return new LocalDate();
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return new LocalDateTime();
    }

    @Override
    public long getNanoTime() {
        return System.nanoTime();
    }
}
