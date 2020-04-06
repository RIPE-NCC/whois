package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Profile({WhoisProfile.TEST})
@Component
public class TestDateTimeProvider implements DateTimeProvider, Stub {
    private LocalDateTime localDateTime = LocalDateTime.now();

    @Override
    public void reset() {
        localDateTime = LocalDateTime.now();
    }

    @Override
    public LocalDate getCurrentDate() {
        return localDateTime.toLocalDate();
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return localDateTime;
    }

    @Override
    public ZonedDateTime getCurrentDateTimeUtc() {
        return getCurrentDateTime().atZone(ZoneOffset.UTC);
    }

    public long getElapsedTime() {
        return 100L;    // return a predictable non-zero value that's testable
    }

    public void setTime(final LocalDateTime dateTime) {
        localDateTime = dateTime;
    }
}
