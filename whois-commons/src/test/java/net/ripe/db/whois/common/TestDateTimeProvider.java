package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.TestingProfile;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

@TestingProfile
@Component
public class TestDateTimeProvider implements DateTimeProvider, Stub {
    private LocalDateTime localDateTime;

    @Override
    public void reset() {
        localDateTime = null;
    }

    public LocalDate getCurrentDate() {
        return localDateTime == null ? LocalDate.now() : localDateTime.toLocalDate();
    }

    public LocalDateTime getCurrentDateTime() {
        return localDateTime == null ? LocalDateTime.now() : localDateTime;
    }

    public void setTime(LocalDateTime dateTime) {
        localDateTime = dateTime;
    }
}
