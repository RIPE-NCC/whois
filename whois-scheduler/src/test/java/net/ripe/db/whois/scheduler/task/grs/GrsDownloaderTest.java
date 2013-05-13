package net.ripe.db.whois.scheduler.task.grs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrsDownloaderTest {
    @Mock GrsSource grsSource;
    @Mock GrsDownloader.AcquireHandler acquireHandler;
    @InjectMocks GrsDownloader subject;

    @Before
    public void setUp() throws Exception {
        when(grsSource.getSource()).thenReturn("RIPE-GRS");
        when(grsSource.getLogger()).thenReturn(LoggerFactory.getLogger(GrsDownloaderTest.class));
    }

    @Test
    public void acquire_success() throws IOException {
        final File file = new File("RIPE-GRS-RES");

        subject.acquire(grsSource, file, acquireHandler);
        verify(acquireHandler).acquire(file);
    }
}
