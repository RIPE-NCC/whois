package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ExportFileWriterFactoryTest {
    private static final int LAST_SERIAL = 1234;

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock DummifierLegacy dummifierLegacy;
    @Mock DummifierCurrent dummifierCurrent;
    ExportFileWriterFactory subject;

    @Before
    public void setup() {
        subject = new ExportFileWriterFactory(dummifierLegacy, dummifierCurrent, "internal", "dbase_new", "dbase");
    }

    @Test(expected = IllegalStateException.class)
    public void createExportFileWriters_existing_dir() throws IOException {
        folder.newFolder("dbase");
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
    }

    @Test
    public void createExportFileWriters() {
        final List<ExportFileWriter> exportFileWriters = subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
        assertThat(exportFileWriters.isEmpty(), Matchers.is(false));

        final File[] files = folder.getRoot().listFiles();
        assertNotNull(files);
        assertThat(files.length, Matchers.is(3));

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
        assertThat(homeDir.exists(), Matchers.is(true));
        assertThat(homeDir.isDirectory(), Matchers.is(true));
        assertThat(homeDir.canWrite(), Matchers.is(true));

        assertThat(subject.isExportDir(homeDir), Matchers.is(false));
    }

    @Test
    public void isExportDir_empty() {
        assertThat(subject.isExportDir(folder.getRoot()), Matchers.is(true));
    }

    @Test
    public void isExportDir_created() {
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);
        assertThat(subject.isExportDir(folder.getRoot()), Matchers.is(true));
    }

    @Test
    public void isLastSerialFile_created() throws IOException {
        subject.createExportFileWriters(folder.getRoot(), LAST_SERIAL);

        final File currentSerialFile = new File(folder.getRoot(), "dbase/RIPE.CURRENTSERIAL");
        assertThat(currentSerialFile.exists(), Matchers.is(true));

        final String savedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), Charsets.UTF_8);
        assertThat(savedSerial, Matchers.is(String.valueOf(LAST_SERIAL)));

        final File newSerialFile = new File(folder.getRoot(), "dbase_new/RIPE.CURRENTSERIAL");
        assertThat(newSerialFile.exists(), Matchers.is(true));

        final String newSavedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), Charsets.UTF_8);
        assertThat(newSavedSerial, Matchers.is(String.valueOf(LAST_SERIAL)));
    }
}
