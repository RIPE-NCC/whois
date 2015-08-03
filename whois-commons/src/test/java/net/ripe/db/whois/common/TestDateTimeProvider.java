package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;

@Profile({WhoisProfile.TEST, WhoisProfile.ENDTOEND})
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
    public DateTime getCurrentDateTimeUtc() {
        return getCurrentDateTime().toDateTime(DateTimeZone.UTC);
    }

    @Override
    public long getNanoTime() {
        return getCurrentDateTime().toDateTime().getMillis();
    }

    public void setTime(LocalDateTime dateTime) {
        localDateTime = dateTime;
    }

    public void setNanoTime(long nanoTime) {
        localDateTime = LocalDateTime.fromDateFields(new Date(nanoTime));
    }
}
