package net.ripe.db.whois.common;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class FormatHelperTest {
    @Test
    public void testDateToString_null() {
        assertNull(FormatHelper.dateToString(null));
    }

    @Test
    public void testDateToString_date() {
        assertThat(FormatHelper.dateToString(new LocalDate(2001, 10, 1)), is("2001-10-01"));
    }

    @Test
    public void testDateToString_dateTime() throws Exception {
        assertThat(FormatHelper.dateToString(new LocalDateTime(2001, 10, 1, 12, 0, 0)), is("2001-10-01"));
    }

    @Test
    public void testDateTimeToString_null() {
        assertNull(FormatHelper.dateTimeToString(null));
    }

    @Test
    public void testDateTimeToString_date() {
        assertThat(FormatHelper.dateTimeToString(new LocalDate(2001, 10, 1)), is("2001-10-01"));
    }

    @Test
    public void testDateTimeToString_dateTime() throws Exception {
        assertThat(FormatHelper.dateTimeToString(new LocalDateTime(2001, 10, 1, 12, 13, 14)), is("2001-10-01 12:13:14"));
    }

}
