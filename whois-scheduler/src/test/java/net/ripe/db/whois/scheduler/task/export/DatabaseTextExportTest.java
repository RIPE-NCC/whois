package net.ripe.db.whois.scheduler.task.export;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTextExportTest {
    @Mock RpslObjectsExporter rpslObjectsExporter;
    @InjectMocks DatabaseTextExport subject;

    @Test
    public void run() {
        subject.run();

        verify(rpslObjectsExporter).export();
    }
}
