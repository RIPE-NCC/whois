package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DailyLogFolderTest {

    @Test(expected = IllegalArgumentException.class)
    public void log_invalid_folder() throws IOException {
        new DailyLogFolder(getLogFolder("/log").toPath());
    }

    @Test
    public void matcherTest() {
        assertFalse(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/000.audit.xml.gz").matches());
        assertFalse(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/001.ack.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-in.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher("/log/update/20130306/123623.428054357.0.1362569782886.JavaMail.andre/003.msg-out.txt.gz").matches());
    }

    private static File getLogFolder(final String path) throws IOException {
        return new ClassPathResource(path).getFile();
    }
}
