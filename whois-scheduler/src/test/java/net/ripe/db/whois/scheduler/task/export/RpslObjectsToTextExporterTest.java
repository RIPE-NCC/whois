package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.task.export.dao.ExportCallbackHandler;
import net.ripe.db.whois.scheduler.task.export.dao.ExportDao;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RpslObjectsToTextExporterTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock ExportFileWriterFactory exportFileWriterFactory;
    @Mock ExportDao exportDao;
    @Mock TagsDao tagsDao;

    RpslObjectsExporter subject;
    File exportDir;
    File tmpDir;

    @Before
    public void setUp() throws Exception {
        exportDir = folder.newFolder("export");
        tmpDir = folder.newFolder("export_tmp");

        final String exportdirName = exportDir.getAbsolutePath();
        final String tmpDirName = tmpDir.getAbsolutePath();

        when(exportFileWriterFactory.isExportDir(any(File.class))).thenReturn(true);

        subject = new RpslObjectsExporter(exportFileWriterFactory, exportDao, tagsDao, exportdirName, tmpDirName, true);
    }

    @Test(expected = RuntimeException.class)
    public void export_invalid_dir() {
        Mockito.reset(exportFileWriterFactory);

        subject.export();
    }

    @Test
    public void export() {
        final int maxSerial = 1234;
        when(exportDao.getMaxSerial()).thenReturn(maxSerial);

        subject.export();

        Mockito.verify(exportFileWriterFactory).createExportFileWriters(tmpDir, maxSerial);
    }

    @Test
    public void export_twice() {
        subject.export();
        subject.export();
    }

    @Test
    public void export_objects() throws IOException {
        final ExportFileWriter exportFileWriter1 = Mockito.mock(ExportFileWriter.class);
        final ExportFileWriter exportFileWriter2 = Mockito.mock(ExportFileWriter.class);
        @SuppressWarnings("unchecked")
        final List<Tag> emptyList = Collections.EMPTY_LIST;

        when(exportFileWriterFactory.createExportFileWriters(tmpDir, 0)).thenReturn(Lists.newArrayList(exportFileWriter1, exportFileWriter2));

        final RpslObject rpslObject1 = RpslObject.parse(2, "mntner: DEV-MNT1");
        final RpslObject rpslObject2 = RpslObject.parse(3, "mntner: DEV-MNT2");

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ExportCallbackHandler exportCallbackHandler = (ExportCallbackHandler) invocation.getArguments()[0];

                for (final RpslObject rpslObject : Lists.newArrayList(rpslObject1, rpslObject2)) {
                    exportCallbackHandler.exportObject(rpslObject);
                }

                return null;
            }
        }).when(exportDao).exportObjects(any(ExportCallbackHandler.class));

        when(tagsDao.getTags(anyInt())).thenReturn(emptyList);

        subject.export();

        Mockito.verify(exportFileWriter1).write(rpslObject1, emptyList);
        Mockito.verify(exportFileWriter1).write(rpslObject2, emptyList);
        Mockito.verify(exportFileWriter1).close();

        Mockito.verify(exportFileWriter2).write(rpslObject1, emptyList);
        Mockito.verify(exportFileWriter2).write(rpslObject2, emptyList);
        Mockito.verify(exportFileWriter2).close();
    }

    @Test
    public void export_objects_exception() throws IOException {
        final ExportFileWriter exportFileWriter = Mockito.mock(ExportFileWriter.class);
        @SuppressWarnings("unchecked")
        final List<Tag> emptyList = Collections.EMPTY_LIST;

        when(exportFileWriterFactory.createExportFileWriters(tmpDir, 0)).thenReturn(Lists.newArrayList(exportFileWriter));

        final RpslObject rpslObject1 = RpslObject.parse(2, "mntner: DEV-MNT1");
        final RpslObject rpslObject2 = RpslObject.parse(3, "mntner: DEV-MNT2");

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ExportCallbackHandler exportCallbackHandler = (ExportCallbackHandler) invocation.getArguments()[0];
                for (final RpslObject rpslObject : Lists.newArrayList(rpslObject1, rpslObject2)) {
                    exportCallbackHandler.exportObject(rpslObject);
                }

                return null;
            }
        }).when(exportDao).exportObjects(any(ExportCallbackHandler.class));

        Mockito.doThrow(IOException.class).when(exportFileWriter).write(rpslObject1, emptyList);

        when(tagsDao.getTags(2)).thenReturn(emptyList);

        try {
            subject.export();
            Assert.fail("Expected exception");
        } catch (RuntimeException ignored) {
        }

        Mockito.verify(exportFileWriter).write(rpslObject1, emptyList);
        Mockito.verify(exportFileWriter, Mockito.never()).write(rpslObject2, emptyList);
        Mockito.verify(exportFileWriter).close();
    }

    @Test
    public void export_check_files() {
        subject.export();

        assertThat(exportDir.exists(), Matchers.is(true));
        assertThat(tmpDir.exists(), Matchers.is(false));
    }

    @Test
    public void execute_multiple_simultaneous() throws Exception {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch waitLatch = new CountDownLatch(1);

        when(exportDao.getMaxSerial()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                startLatch.countDown();
                waitLatch.await(5, TimeUnit.SECONDS);
                return 0;
            }
        });

        new Thread() {
            @Override
            public void run() {
                subject.export();
            }
        }.start();

        try {
            startLatch.await(5, TimeUnit.SECONDS);
            subject.export();
            Assert.fail("Expected exception");
        } catch (IllegalStateException ignored) {
        } finally {
            waitLatch.countDown();
        }
    }
}
