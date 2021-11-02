package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class ExportFileWriterFactoryTest {
    private static final int LAST_SERIAL = 1234;

    @TempDir
    public File folder;

    @Mock
    DummifierNrtm dummifierNrtm;
    @Mock DummifierCurrent dummifierCurrent;
    ExportFileWriterFactory subject;

    @BeforeEach
    public void setup() {
        subject = new ExportFileWriterFactory(dummifierNrtm, dummifierCurrent, "internal", "dbase_new", "dbase", "test", "test-nonauth");
    }

    @Test
    @Disabled("TODO: [MA] Junit 5 migration, works on local machine, not in gitlab.")
    public void createExportFileWriters_existing_dir() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new File(folder, "export");
            subject.createExportFileWriters(folder.toPath().getRoot().toFile(), LAST_SERIAL);
        });
    }

    @Test
    public void createExportFileWriters() {
        final List<ExportFileWriter> exportFileWriters = subject.createExportFileWriters(folder, LAST_SERIAL);
        assertThat(exportFileWriters.isEmpty(), is(false));

        final File[] files = folder.listFiles();
        assertNotNull(files);
        assertThat(files.length, is(3));

        for (final File file : files) {
            if (! (file.getAbsolutePath().endsWith("internal")
                    || file.getAbsolutePath().endsWith("dbase")
                    || file.getAbsolutePath().endsWith("dbase_new"))) {
                Assertions.fail("Unexpected folder: " + file.getAbsolutePath());
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
        assertThat(subject.isExportDir(folder), is(true));
    }

    @Test
    public void isExportDir_created() {
        subject.createExportFileWriters(folder, LAST_SERIAL);
        assertThat(subject.isExportDir(folder), is(true));
    }

    @Test
    public void isLastSerialFile_created() throws IOException {
        subject.createExportFileWriters(folder, LAST_SERIAL);

        final File currentSerialFile = new File(folder, "dbase/RIPE.CURRENTSERIAL");
        assertThat(currentSerialFile.exists(), is(true));

        final String savedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(savedSerial, is(String.valueOf(LAST_SERIAL)));

        final File newSerialFile = new File(folder, "dbase_new/RIPE.CURRENTSERIAL");
        assertThat(newSerialFile.exists(), is(true));

        final String newSavedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(newSavedSerial, is(String.valueOf(LAST_SERIAL)));
    }
}
