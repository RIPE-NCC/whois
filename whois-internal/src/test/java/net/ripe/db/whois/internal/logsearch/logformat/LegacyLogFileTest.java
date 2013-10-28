package net.ripe.db.whois.internal.logsearch.logformat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.regex.Matcher;

import static net.ripe.db.whois.internal.logsearch.logformat.LegacyLogFile.LOGSECTION_PATTERN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class LegacyLogFileTest {

    private String logDirectory;

    @Before
    public void setup() throws IOException {
        logDirectory = new ClassPathResource("/log/legacy").getFile().getAbsolutePath();
    }

    @Test
    public void validFilenamePattern() {
        final LegacyLogFile legacyLogFile = new LegacyLogFile(logDirectory + "/updlog.20030726.bz2");

        assertThat(legacyLogFile.getDate(), is("20030726"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFilenamePattern() {
        new LegacyLogFile("updlog.20040404");
    }

    @Test
    public void logStartPatternTest() {
        final Matcher matcher = LOGSECTION_PATTERN.matcher(">>> time: Sat Jul 26 23:33:15 2003 SYNC UPDATE (193.0.109.35) <<<");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group(1), is("23:33:15"));
        assertThat(matcher.group(2), is("SYNC UPDATE"));
        assertThat(matcher.group(4), is("(193.0.109.35)"));
    }

    @Test
    public void logStartMailPatternTest() {
        final Matcher matcher = LOGSECTION_PATTERN.matcher(">>> time: Sat Jul 26 23:33:15 2003 MAIL UPDATE <<<");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group(1), is("23:33:15"));
        assertThat(matcher.group(2), is("MAIL UPDATE"));
        assertThat(matcher.group(3), is(nullValue()));
        assertThat(matcher.group(4), is(nullValue()));
    }

    @Test
    public void logStartMailAckPatternTest() {
        final Matcher matcher = LOGSECTION_PATTERN.matcher(">>> Time: Wed Jul  6 00:00:03 2011 SYNC UPDATE ACK (78.110.160.234) <<<");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group(1), is("00:00:03"));
        assertThat(matcher.group(2), is("SYNC UPDATE"));
        assertThat(matcher.group(3), is("ACK"));
        assertThat(matcher.group(4), is("(78.110.160.234)"));
    }

    @Test
    public void logStartSyncupdateAckPatternTest() {
        final Matcher matcher = LOGSECTION_PATTERN.matcher(">>> Time: Wed Jul  6 00:05:00 2011 MAIL UPDATE ACK <<<");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group(1), is("00:05:00"));
        assertThat(matcher.group(2), is("MAIL UPDATE"));
        assertThat(matcher.group(3), is("ACK"));
        assertThat(matcher.group(4), is(nullValue()));
    }
}
