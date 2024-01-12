package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DateUtilTest {

    private static final Long EPOCH_TIMESTAMP = 0L;
    private static final Long RECENT_TIMESTAMP = 1388617200L * 1_000L;

    private static final LocalDateTime EPOCH_LOCAL_DATE_TIME = localDateTime(EPOCH_TIMESTAMP);
    private static final LocalDateTime RECENT_LOCAL_DATE_TIME = localDateTime(RECENT_TIMESTAMP);

    @Test
    public void fromDate() {
        assertThat(DateUtil.fromDate(new Date(EPOCH_TIMESTAMP)), is(EPOCH_LOCAL_DATE_TIME));
        assertThat(DateUtil.fromDate(new Date(RECENT_TIMESTAMP)), is(RECENT_LOCAL_DATE_TIME));
    }

    @Test
    public void toDate() {
        assertThat(DateUtil.toDate(EPOCH_LOCAL_DATE_TIME), is(new Date(EPOCH_TIMESTAMP)));
        assertThat(DateUtil.toDate(RECENT_LOCAL_DATE_TIME), is(new Date(RECENT_TIMESTAMP)));
    }

    // helper methods

    // convert from timestamp (in milliseconds since unix epoch) into a LocalDateTime
    private static LocalDateTime localDateTime(final long value) {
        return Instant.ofEpochMilli(value).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }
}
