package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class FormatHelperTest {
    @Test
    public void testDateToString_null() {
        assertThat(FormatHelper.dateToString(null), is(nullValue()));
    }

    @Test
    public void testDateToString_date() {
        assertThat(FormatHelper.dateToString(LocalDate.of(2001, 10, 1)), is("2001-10-01"));
    }

    @Test
    public void testDateToString_dateTime() throws Exception {
        assertThat(FormatHelper.dateToString(LocalDateTime.of(2001, 10, 1, 12, 0, 0)), is("2001-10-01"));
    }

    @Test
    public void testDateTimeToString_null() {
        assertThat(FormatHelper.dateTimeToString(null), is(nullValue()));
    }

    @Test
    public void testDateTimeToString_date() {
        assertThat(FormatHelper.dateTimeToString(LocalDate.of(2001, 10, 1)), is("2001-10-01"));
    }

    @Test
    public void testDateTimeToString_dateTime() throws Exception {
        assertThat(FormatHelper.dateTimeToString(LocalDateTime.of(2001, 10, 1, 12, 13, 14)), is("2001-10-01 12:13:14"));
    }

    @Test
    public void testDayDateTimeToString_dateTime() throws Exception {
        assertThat(FormatHelper.dayDateTimeToUtcString(LocalDateTime.of(2001, 10, 1, 12, 13, 14)), is("Mon Oct 1 12:13:14 2001Z"));
    }

}
