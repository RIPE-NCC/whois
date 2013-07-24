package net.ripe.db.whois.wsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogFileSearchJmxTest {
    @Mock LogFileIndex logFileIndex;
    @InjectMocks LogFileSearchJmx subject;

    @Test
    public void incrementalImport() throws Exception {
        subject.incrementalImport();

        verify(logFileIndex).update();
    }

    @Test
    public void fullImport() throws Exception {
        subject.fullImport();

        verify(logFileIndex).rebuild();
    }
}
