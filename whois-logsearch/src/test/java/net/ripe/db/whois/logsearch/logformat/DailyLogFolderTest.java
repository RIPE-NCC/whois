package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static net.ripe.db.whois.logsearch.logformat.LoggedUpdate.Type.DAILY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DailyLogFolderTest {

    @Test(expected = IllegalArgumentException.class)
    public void log_invalid_folder() throws IOException {
        new DailyLogFolder(getLogFolder("/log").toPath());
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
    public void matcherTest() {
        assertFalse(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/000.audit.xml.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/001.ack.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-in.txt.gz").matches());
        assertFalse(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/003.msg-out.txt.gz").matches());
    }
}
