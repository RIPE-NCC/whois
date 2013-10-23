package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.logsearch.jmx.LogFileUpdateJmx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogFileUpdateJmxTest {
    @Mock LogFileIndex logFileIndex;
    @Mock LegacyLogFormatProcessor legacyLogFormatProcessor;
    @Mock NewLogFormatProcessor newLogFormatProcessor;
    @InjectMocks LogFileUpdateJmx subject;

    @Test
    public void indexLegacyFile() throws Exception {
        String path = new ClassPathResource("log/legacy/acklog.20110506.bz2").getFile().getAbsolutePath();
        subject.indexLegacyFile(path);

        verify(legacyLogFormatProcessor).addFileToIndex(path);
    }

    @Test
    public void indexLegacyDirectory() throws Exception {
        String path = new ClassPathResource("log/legacy").getFile().getAbsolutePath();
        subject.indexLegacyDirectory(path);

        verify(legacyLogFormatProcessor).addDirectoryToIndex(path);
    }

    @Test
    public void indexLegacy_pathcheck() throws Exception {
        final String path = "/var/log/legacy/20120505.tar";
        subject.indexLegacyFile(path);

        verify(legacyLogFormatProcessor, never()).addFileToIndex(path);
    }

    @Test
    public void indexNewLog_path_does_not_exist() throws Exception {
        final String path = "/var/log/update/20120505.tar";
        subject.indexDailyLogFile(path);

        verify(newLogFormatProcessor, never()).addFileToIndex(path);
    }

    @Test
    public void indexNewLog_path_is_not_a_file() throws Exception {
        final String path = "/var/log/update";
        subject.indexDailyLogFile(path);

        verify(newLogFormatProcessor, never()).addFileToIndex(path);
    }

    @Test
    public void indexNewLog() throws Exception {
        String path = new ClassPathResource("log/update/20130305.tar").getFile().getAbsolutePath();
        subject.indexDailyLogFile(path);

        verify(newLogFormatProcessor).addFileToIndex(path);
    }
}
