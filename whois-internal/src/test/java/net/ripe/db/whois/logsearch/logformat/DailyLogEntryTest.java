package net.ripe.db.whois.logsearch.logformat;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class DailyLogEntryTest {
    @Test
    public void parse_update() {
        final DailyLogEntry dailyLogEntry = new DailyLogEntry("20130312/101010.update/001.msg-in.txt.gz", "20130312");
        assertThat(dailyLogEntry.getUpdateId(), is("20130312/101010.update/001.msg-in.txt.gz"));
        assertThat(dailyLogEntry.getDate(), is("20130312"));
    }

    @Test
    public void parse_ack() {
        final DailyLogEntry dailyLogEntry = new DailyLogEntry("20130312/101010.update/002.ack.txt.gz", "20130312");
        assertThat(dailyLogEntry.getUpdateId(), is("20130312/101010.update/002.ack.txt.gz"));
        assertThat(dailyLogEntry.getDate(), is("20130312"));
    }

    @Test
    public void parse_notification() {
        final DailyLogEntry dailyLogEntry = new DailyLogEntry("20130312/101010.update/666.msg-out.txt.gz", "20130312");
        assertThat(dailyLogEntry.getUpdateId(), is("20130312/101010.update/666.msg-out.txt.gz"));
        assertThat(dailyLogEntry.getDate(), is("20130312"));
    }
}
