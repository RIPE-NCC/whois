package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GrsImporterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock GrsSourceImporter grsSourceImporter;
    @Mock GrsSource grsSourceRipe;
    @Mock GrsSource grsSourceOther;
    @Mock GrsDao grsDao;
    @Mock GrsDao.UpdateResult updateResult;

    String defaultSources = "RIPE-GRS,OTHER-GRS";

    Logger logger = LoggerFactory.getLogger(GrsImporter.class);

    GrsImporter subject;

    @Before
    public void setUp() throws Exception {
        when(grsSourceRipe.getName()).thenReturn(ciString("RIPE-GRS"));
        when(grsSourceRipe.getLogger()).thenReturn(logger);
        when(grsSourceRipe.getDao()).thenReturn(grsDao);

        when(grsSourceOther.getName()).thenReturn(ciString("OTHER-GRS"));
        when(grsSourceOther.getLogger()).thenReturn(logger);
        when(grsSourceOther.getDao()).thenReturn(grsDao);

        when(grsDao.createObject(any(RpslObject.class))).thenReturn(updateResult);
        when(grsDao.updateObject(any(GrsObjectInfo.class), any(RpslObject.class))).thenReturn(updateResult);

        subject = new GrsImporter(grsSourceImporter, new GrsSource[]{grsSourceRipe, grsSourceOther});
        subject.setDefaultSources(defaultSources);
        subject.setGrsImportEnabled(true);

        subject.startImportThreads();
    }

    @After
    public void tearDown() throws Exception {
        subject.shutdownImportThreads();
    }

    @Test
    public void run() {
        subject = spy(subject);
        subject.setGrsImportEnabled(false);
        subject.run();
        verify(subject, times(0)).grsImport(anyString(), anyBoolean());
    }

    @Test
    public void grsImport_not_enabled() throws Exception {
        subject.setGrsImportEnabled(false);
        subject.run();

        verifyZeroInteractions(grsSourceImporter);
        verify(grsSourceRipe, never()).acquireDump(any(File.class));
        verify(grsSourceRipe, never()).handleObjects(any(File.class), any(ObjectHandler.class));
        verify(grsSourceOther, never()).acquireDump(any(File.class));
        verify(grsSourceOther, never()).handleObjects(any(File.class), any(ObjectHandler.class));
    }

    @Test
    public void grsImport_RIPE_GRS_no_rebuild() throws Exception {
        await(subject.grsImport("RIPE-GRS", false));

        verify(grsSourceImporter).grsImport(grsSourceRipe, false);
        verify(grsSourceImporter, never()).grsImport(grsSourceOther, false);
    }

    @Test
    public void grsImport_RIPE_GRS_rebuild() throws Exception {
        await(subject.grsImport("RIPE-GRS", true));

        verify(grsSourceImporter).grsImport(grsSourceRipe, true);
        verify(grsSourceImporter, never()).grsImport(grsSourceOther, true);
    }

    @Test
    public void grsImport_unknown_source() throws Exception {
        await(subject.grsImport("UNKNOWN-GRS", true));

        verify(grsSourceRipe, never()).acquireDump(any(File.class));
        verify(grsSourceRipe, never()).handleObjects(any(File.class), any(ObjectHandler.class));

        verify(grsDao, never()).cleanDatabase();
    }

    @Test
    public void grsImport_RIPE_GRS_acquire_fails() throws Exception {
        doThrow(RuntimeException.class).when(grsSourceRipe).acquireDump(any(File.class));

        await(subject.grsImport("RIPE-GRS", false));

        verify(grsSourceRipe, never()).handleObjects(any(File.class), any(ObjectHandler.class));
    }

    private void await(final List<Future<?>> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }
}
