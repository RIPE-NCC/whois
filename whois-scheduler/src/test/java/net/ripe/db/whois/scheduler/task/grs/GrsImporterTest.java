package net.ripe.db.whois.scheduler.task.grs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GrsImporterTest {

    @TempDir
    public File folder;

    @Mock GrsSourceImporter grsSourceImporter;
    @Mock GrsSource grsSourceRipe;
    @Mock GrsSource grsSourceOther;
    @Mock GrsDao grsDao;
    @Mock GrsDao.UpdateResult updateResult;

    String defaultSources = "RIPE-GRS,OTHER-GRS";

    Logger logger = LoggerFactory.getLogger(GrsImporter.class);

    GrsImporter subject;

    @BeforeEach
    public void setUp() throws Exception {
        when(grsSourceRipe.getName()).thenReturn(ciString("RIPE-GRS"));
        when(grsSourceOther.getName()).thenReturn(ciString("OTHER-GRS"));

        subject = new GrsImporter(grsSourceImporter, new GrsSource[]{grsSourceRipe, grsSourceOther});

        subject.startImportThreads();
    }

    @AfterEach
    public void tearDown() throws Exception {
        subject.shutdownImportThreads();
    }

    @Test
    public void grsImport_RIPE_GRS_no_rebuild() throws Exception {
        when(grsSourceRipe.getLogger()).thenReturn(logger);
        await(subject.grsImport("RIPE-GRS", false));

        verify(grsSourceImporter).grsImport(grsSourceRipe, false);
        verify(grsSourceImporter, never()).grsImport(grsSourceOther, false);
    }

    @Test
    public void grsImport_RIPE_GRS_rebuild() throws Exception {
        when(grsSourceRipe.getLogger()).thenReturn(logger);
        await(subject.grsImport("RIPE-GRS", true));

        verify(grsSourceImporter).grsImport(grsSourceRipe, true);
        verify(grsSourceImporter, never()).grsImport(grsSourceOther, true);
    }

    @Test
    public void grsImport_unknown_source() throws Exception {
        await(subject.grsImport("UNKNOWN-GRS", true));

        verify(grsSourceRipe, never()).acquireDump(any(Path.class));
        verify(grsSourceRipe, never()).handleObjects(any(File.class), any(ObjectHandler.class));

        verify(grsDao, never()).cleanDatabase();
    }

    @Test
    public void grsImport_RIPE_GRS_acquire_fails() throws Exception {
        when(grsSourceRipe.getLogger()).thenReturn(logger);
        await(subject.grsImport("RIPE-GRS", false));

        verify(grsSourceRipe, never()).handleObjects(any(File.class), any(ObjectHandler.class));
    }

    private void await(final List<Future> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }
}
