package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ExportFileWriterFactoryTest {
    private static final int LAST_SERIAL = 1234;

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    DummifierNrtm dummifierNrtm;
    @Mock DummifierCurrent dummifierCurrent;
    ExportFileWriterFactory subject;

    @Before
    public void setup() {
        subject = new ExportFileWriterFactory(dummifierNrtm, dummifierCurrent, "internal", "dbase_new", "dbase", "test", "test-nonauth");
    }

    @Test(expected = IllegalStateException.class)
    public void createExportFileWriters_existing_dir() throws IOException {
        folder.newFolder("dbase");
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
    }

    @Test
    public void createExportFileWriters() {
        final List<ExportFileWriter> exportFileWriters = subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
        assertThat(exportFileWriters.isEmpty(), is(false));

        final File[] files = folder.getRoot().listFiles();
        assertNotNull(files);
        assertThat(files.length, is(3));

        for (final File file : files) {
            if (! (file.getAbsolutePath().endsWith("internal")
                    || file.getAbsolutePath().endsWith("dbase")
                    || file.getAbsolutePath().endsWith("dbase_new"))) {
                Assert.fail("Unexpected folder: " + file.getAbsolutePath());
            }
        }
    }

    @Test
    public void isExportDir_home() {
        final File homeDir = new File(System.getProperty("user.home"));
        assertThat(homeDir.exists(), is(true));
        assertThat(homeDir.isDirectory(), is(true));
        assertThat(homeDir.canWrite(), is(true));

        assertThat(subject.isExportDir(homeDir), is(false));
    }

    @Test
    public void isExportDir_empty() {
        assertThat(subject.isExportDir(folder.getRoot()), is(true));
    }

    @Test
    public void isExportDir_created() {
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
        assertThat(subject.isExportDir(folder.getRoot()), is(true));
    }

    @Test
    public void isLastSerialFile_created() throws IOException {
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);

        final File currentSerialFile = new File(folder.getRoot(), "dbase/RIPE.CURRENTSERIAL");
        assertThat(currentSerialFile.exists(), is(true));

        final String savedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(savedSerial, is(String.valueOf(LAST_SERIAL)));

        final File newSerialFile = new File(folder.getRoot(), "dbase_new/RIPE.CURRENTSERIAL");
        assertThat(newSerialFile.exists(), is(true));

        final String newSavedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(newSavedSerial, is(String.valueOf(LAST_SERIAL)));
    }
}
