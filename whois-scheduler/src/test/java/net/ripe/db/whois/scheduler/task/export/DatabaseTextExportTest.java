package net.ripe.db.whois.scheduler.task.export;

import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DatabaseTextExportTest {
    @Mock RpslObjectsExporter rpslObjectsExporter;
    @InjectMocks DatabaseTextExport subject;

    @Test
    public void run() {
        subject.run();

        verify(rpslObjectsExporter).export();
    }
}
