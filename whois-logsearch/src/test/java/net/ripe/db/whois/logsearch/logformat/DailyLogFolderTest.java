package net.ripe.db.whois.logsearch.logformat;

import com.google.common.collect.Maps;
import net.ripe.db.whois.logsearch.LoggedUpdateProcessor;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.logsearch.logformat.LoggedUpdate.Type.DAILY;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DailyLogFolderTest {

    @Test(expected = IllegalArgumentException.class)
    public void log_invalid_folder() throws IOException {
        new DailyLogFolder(getLogFolder("/log"));
    }

    @Test
    public void process_logs_in_folder() throws IOException {
        final Map<DailyLogEntry, String> logs = getDailyLogEntries("/log/update/20130306");

        final Set<DailyLogEntry> updateInfos = logs.keySet();
        assertThat(updateInfos, hasSize(6));
        final Iterator<DailyLogEntry> updateInfoIterator = updateInfos.iterator();

        final String date = "20130306";
        DailyLogEntry nextDailyLog = updateInfoIterator.next();
        validateDailyLogEntry(nextDailyLog, date, nextDailyLog.getUpdateId());

        nextDailyLog = updateInfoIterator.next();
        validateDailyLogEntry(nextDailyLog, date, nextDailyLog.getUpdateId());

        nextDailyLog = updateInfoIterator.next();
        validateDailyLogEntry(nextDailyLog, date, nextDailyLog.getUpdateId());
    }

    @Test
    public void daily_log_folders() throws IOException {
        final List<DailyLogFolder> dailyLogFolders = DailyLogFolder.getDailyLogFolders(getLogFolder("/log/update").toPath(), 0, System.currentTimeMillis());

        assertThat(dailyLogFolders, hasSize(1));
        assertThat(dailyLogFolders.get(0), is(new DailyLogFolder(getLogFolder("/log/update/20130306"))));
    }

    private Map<DailyLogEntry, String> getDailyLogEntries(final String path) throws IOException {
        final Map<DailyLogEntry, String> logs = Maps.newLinkedHashMap();

        new DailyLogFolder(getLogFolder(path)).processLoggedFiles(new LoggedUpdateProcessor() {
            @Override
            public boolean accept(final LoggedUpdate loggedUpdate) {
                return true;
            }

            @Override
            public void process(final LoggedUpdate loggedUpdate, String contents) {
                logs.put((DailyLogEntry)loggedUpdate, contents);
            }
        });
        return logs;
    }

    private static void validateDailyLogEntry(final DailyLogEntry dailyLogEntry, final String date, final String filename) {
        assertThat(dailyLogEntry.getUpdateId(), is(filename));
        assertThat(dailyLogEntry.getDate(), is(date));
        assertThat(dailyLogEntry.getType(), is(DAILY));
    }

    private static File getLogFolder(final String path) throws IOException {
        return new ClassPathResource(path).getFile();
    }

    @Test
    public void matcherTest_audit() {
        File pathname = new File("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/000.audit.xml.gz");
        assertThat(DailyLogEntry.UPDATE_LOG_FILE_PATTERN.matcher(pathname.getName()).matches(), is(true));
    }

    @Test
    public void matcherTest_in_out() {
        File pathname = new File("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/001.msg-in.txt.gz");
        assertThat(DailyLogEntry.UPDATE_LOG_FILE_PATTERN.matcher(pathname.getName()).matches(), is(true));
    }
}
