package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({WhoisProfile.TEST, WhoisProfile.ENDTOEND})
@Component
public class TestDateTimeProvider implements DateTimeProvider, Stub {
    private LocalDateTime localDateTime;
    private long nanoTime;

    @Override
    public void reset() {
        localDateTime = null;
        nanoTime = 0;
    }

    @Override
    public LocalDate getCurrentDate() {
        return localDateTime == null ? LocalDate.now() : localDateTime.toLocalDate();
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return localDateTime == null ? LocalDateTime.now() : localDateTime;
    }

    @Override
    public long getNanoTime() {
        return nanoTime;
    }

    public void setTime(LocalDateTime dateTime) {
        localDateTime = dateTime;
    }

    public void setNanoTime(long nanoTime) {
        nanoTime = nanoTime;
    }
}
