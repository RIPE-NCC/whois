package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.task.export.dao.ExportCallbackHandler;
import net.ripe.db.whois.scheduler.task.export.dao.ExportDao;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class RpslObjectsToTextExporterTest {
    @TempDir
    Path folder;

    @Mock ExportFileWriterFactory exportFileWriterFactory;
    @Mock ExportDao exportDao;
    RpslObjectsExporter subject;
    File exportDir;
    File tmpDir;

    @BeforeEach
    public void setUp() throws Exception {
        exportDir = Files.createDirectories(folder.resolve("export")).toFile();
        tmpDir = Files.createDirectories(folder.resolve( "export_tmp")).toFile();

        when(exportFileWriterFactory.isExportDir(any(File.class))).thenReturn(true);

        subject = new RpslObjectsExporter(exportFileWriterFactory, exportDao, exportDir.toPath().toString(), tmpDir.toPath().toString(), true);
    }

    //TempDir is not cleaned up properly after each test run.
    @AfterEach
    public void cleanUp()  {
        exportDir.delete();
        tmpDir.delete();
    }

    @Test
    public void export_invalid_dir() {
        assertThrows(RuntimeException.class, () -> {
            Mockito.reset(exportFileWriterFactory);

            subject.export();
        });
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

        subject.export();

        Mockito.verify(exportFileWriter1).write(rpslObject1);
        Mockito.verify(exportFileWriter1).write(rpslObject2);
        Mockito.verify(exportFileWriter1).close();

        Mockito.verify(exportFileWriter2).write(rpslObject1);
        Mockito.verify(exportFileWriter2).write(rpslObject2);
        Mockito.verify(exportFileWriter2).close();
    }

    @Test
    public void export_objects_exception() throws IOException {
        final ExportFileWriter exportFileWriter = Mockito.mock(ExportFileWriter.class);
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

        Mockito.doThrow(IOException.class).when(exportFileWriter).write(rpslObject1);

        try {
            subject.export();
            fail("Expected exception");
        } catch (RuntimeException ignored) {
            // expected
        }

        Mockito.verify(exportFileWriter).write(rpslObject1);
        Mockito.verify(exportFileWriter, Mockito.never()).write(rpslObject2);
        Mockito.verify(exportFileWriter).close();
    }

    @Test
    public void export_check_files() {
        subject.export();

        assertThat(exportDir.exists(), is(true));
        assertThat(tmpDir.exists(), is(false));
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
            fail("Expected exception");
        } catch (IllegalStateException ignored) {
            // expected
        } finally {
            waitLatch.countDown();
        }
    }
}
