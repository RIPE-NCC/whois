package net.ripe.db.whois.wsearch;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LoggedUpdateInfoTest {
    @Test(expected = IllegalArgumentException.class)
    public void parse_empty() {
        LoggedUpdateInfo.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_invalid() {
        LoggedUpdateInfo.parse("/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_no_date_folder() {
        LoggedUpdateInfo.parse("/101010.update");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_no_update_folder() {
        LoggedUpdateInfo.parse("20130312/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_no_filename() {
        LoggedUpdateInfo.parse("20130312/101010.update");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_invalid_filename() {
        LoggedUpdateInfo.parse("20130312/101010.update/filename");
    }

    @Test
    public void parse_audit() {
        final LoggedUpdateInfo updateInfo = LoggedUpdateInfo.parse("/some/ignored/path/prefix/20130312/101010.update/000.audit.gz");
        assertThat(updateInfo.getLoggedUpdateId().getDailyLogFolder(), is("20130312"));
        assertThat(updateInfo.getLoggedUpdateId().getUpdateFolder(), is("101010.update"));
        assertThat(updateInfo.getFilename(), is("000.audit.gz"));
        assertThat(updateInfo.getType(), is(LoggedUpdateInfo.Type.AUDIT));
    }

    @Test
    public void parse_update() {
        final LoggedUpdateInfo updateInfo = LoggedUpdateInfo.parse("20130312/101010.update/001.in.gz");
        assertThat(updateInfo.getType(), is(LoggedUpdateInfo.Type.UPDATE));
    }

    @Test
    public void parse_ack() {
        final LoggedUpdateInfo updateInfo = LoggedUpdateInfo.parse("20130312/101010.update/002.ack.gz");
        assertThat(updateInfo.getType(), is(LoggedUpdateInfo.Type.ACK));
    }

    @Test
    public void parse_notification() {
        final LoggedUpdateInfo updateInfo = LoggedUpdateInfo.parse("20130312/101010.update/666.out.gz");
        assertThat(updateInfo.getType(), is(LoggedUpdateInfo.Type.EMAIL));
    }

    @Test
    public void isLoggedUpdateInfo() {
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("000.gz"), is(true));
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("001.gz"), is(true));
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("002.gz"), is(true));
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("02.gz"), is(true));
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("000.xml"), is(false));
        assertThat(LoggedUpdateInfo.isLoggedUpdateInfo("test.gz"), is(false));
    }

    @Test
    public void string() {
        final String s = LoggedUpdateInfo.parse("/test/20130312/101010.update/666.out.gz").toString();
        assertThat(s, is("[EMAIL       ] 20130312/101010.update/666.out.gz"));
    }
}
