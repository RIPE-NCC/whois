package net.ripe.db.whois.common;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateTimeProviderTest {

    @Test
    public void fromEpochMilli() {
        assertThat(DateTimeProvider.fromEpochMilli(0L), is(LocalDateTime.parse("1970-01-01T01:00")));
        assertThat(DateTimeProvider.fromEpochMilli(1388617200000L), is(LocalDateTime.parse("2014-01-02T00:00")));
    }

    @Test
    public void toEpochMilli() {
        assertThat(DateTimeProvider.toEpochMilli(LocalDateTime.parse("1970-01-01T01:00")), is(0L));
        assertThat(DateTimeProvider.toEpochMilli(LocalDateTime.parse("2014-01-02T00:00")), is(1388617200000L));
    }

    @Test
    public void fromDate() {
        assertThat(DateTimeProvider.fromDate(new Date(0L)), is(LocalDateTime.parse("1970-01-01T01:00")));
    }

    @Test
    public void toDate() {
        assertThat(DateTimeProvider.toDate(LocalDateTime.parse("1970-01-01T01:00")), is(new Date(0L)));
        assertThat(DateTimeProvider.toDate(LocalDateTime.parse("2014-01-02T00:00")), is(new Date(1388617200000L)));
    }
}
