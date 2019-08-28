package net.ripe.db.whois.common;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateTimeProviderTest {

    private static final Long EPOCH_TIMESTAMP = 0L;
    private static final Long RECENT_TIMESTAMP = 1388617200000L;

    private static final LocalDateTime EPOCH_LOCAL_DATE_TIME = localDateTime(EPOCH_TIMESTAMP);
    private static final LocalDateTime RECENT_LOCAL_DATE_TIME = localDateTime(RECENT_TIMESTAMP);

    @Test
    public void fromDate() {
        assertThat(DateTimeProvider.fromDate(new Date(EPOCH_TIMESTAMP)), is(EPOCH_LOCAL_DATE_TIME));
        assertThat(DateTimeProvider.fromDate(new Date(RECENT_TIMESTAMP)), is(RECENT_LOCAL_DATE_TIME));
    }

    @Test
    public void toDate() {
        assertThat(DateTimeProvider.toDate(EPOCH_LOCAL_DATE_TIME), is(new Date(EPOCH_TIMESTAMP)));
        assertThat(DateTimeProvider.toDate(RECENT_LOCAL_DATE_TIME), is(new Date(RECENT_TIMESTAMP)));
    }

    // helper methods

    private static LocalDateTime localDateTime(final long value) {
        return Instant.ofEpochMilli(value).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }
}
