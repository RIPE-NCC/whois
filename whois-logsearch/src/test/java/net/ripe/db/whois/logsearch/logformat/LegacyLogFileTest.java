package net.ripe.db.whois.logsearch.logformat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.regex.Matcher;

import static net.ripe.db.whois.logsearch.logformat.LegacyLogFile.LOGSECTION_PATTERN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class LegacyLogFileTest {

    private String logDirectory;

    @Before
    public void setup() throws IOException {
        logDirectory = new ClassPathResource("/log/legacy").getFile().getAbsolutePath();
    }

//    @Ignore
//    @Test
//    public void readLogs() throws IOException {
//        final List<LegacyLogEntry> logs = Lists.newArrayList();
//        subject.processLogFile(new LoggedUpdateProcessor() {
//
//            @Override
//            public boolean accept(LoggedUpdate loggedUpdateInfo) {
//                return true;
//            }
//
//            @Override
//            public void process(final LoggedUpdate loggedUpdate, final String contents) {
//                logs.add((LegacyLogEntry)loggedUpdate);
//            }
//        });
//
//        assertThat(logs, hasSize(7));
//
//        LegacyLogEntry logEntry0 = Iterables.get(logs, 0);
//        assertThat(logEntry0.getDate(), is("20110506"));
//        assertThat(logEntry0.getType(), is(LoggedUpdate.Type.LEGACY));
//        assertThat(logEntry0.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/acklog.20110506.bz2/0"));
//
//        LegacyLogEntry logEntry1 = Iterables.get(logs, 1);
//        assertThat(logEntry1.getDate(), is("20110506"));
//        assertThat(logEntry1.getType(), is(LoggedUpdate.Type.LEGACY));
//        assertThat(logEntry1.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/acklog.20110506.bz2/1"));
//
//        LegacyLogEntry logEntry2 = Iterables.get(logs, 2);
//        assertThat(logEntry2.getDate(), is("20110506"));
//        assertThat(logEntry2.getType(), is(LoggedUpdate.Type.LEGACY));
//        assertThat(logEntry2.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/acklog.20110506.bz2/2"));
//
//        LegacyLogEntry helpLog = Iterables.get(logs, 3);
//        assertThat(helpLog.getDate(), is("20030726"));
//        assertThat(helpLog.getType(), is(LoggedUpdate.Type.LEGACY));
//        assertThat(helpLog.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/updlog.20030726.bz2/0"));
//
//        LegacyLogEntry legacyLog = Iterables.get(logs, 4);
//        assertThat(legacyLog.getDate(), is("20030726"));
//        assertThat(legacyLog.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/updlog.20030726.bz2/1"));
//        assertThat(legacyLog.getType(), is(LoggedUpdate.Type.LEGACY));
//
//        legacyLog = Iterables.get(logs, 5);
//        assertThat(legacyLog.getDate(), is("20030726"));
//        assertThat(legacyLog.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/updlog.20030726.bz2/2"));
//        assertThat(legacyLog.getType(), is(LoggedUpdate.Type.LEGACY));
//
//        LegacyLogEntry syncAckLog = Iterables.get(logs, 6);
//        assertThat(syncAckLog.getDate(), is("20030726"));
//        assertThat(syncAckLog.getType(), is(LoggedUpdate.Type.LEGACY));
//        assertThat(syncAckLog.getUpdateId(), is(new File(logDir).getCanonicalPath() + "/updlog.20030726.bz2/3"));
//
//    }

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
