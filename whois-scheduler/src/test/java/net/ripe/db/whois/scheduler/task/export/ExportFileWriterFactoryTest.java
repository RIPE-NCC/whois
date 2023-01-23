package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class ExportFileWriterFactoryTest {
    private static final int LAST_SERIAL = 1234;

    @TempDir
    public Path folder;

    @Mock
    DummifierNrtm dummifierNrtm;
    @Mock DummifierCurrent dummifierCurrent;
    ExportFileWriterFactory subject;

    @BeforeEach
    public void setup() {
        subject = new ExportFileWriterFactory(dummifierNrtm, dummifierCurrent, "internal", "dbase_new", "dbase", "test", "test-nonauth");
    }

    @Test
    public void createExportFileWriters_existing_dir() throws IOException {
        Files.createDirectories(folder.resolve("dbase"));

        assertThrows(IllegalStateException.class, () -> {
            subject.createExportFileWriters(folder.toFile(), LAST_SERIAL);
        });
    }

    @Test
    public void createExportFileWriters() {
        final List<ExportFileWriter> exportFileWriters = subject.createExportFileWriters(folder.toFile(), LAST_SERIAL);
        assertThat(exportFileWriters.isEmpty(), is(false));

        final File[] files = folder.toFile().listFiles();
        assertThat(files, not(nullValue()));
        assertThat(files.length, is(3));

        for (final File file : files) {
            if (! (file.getAbsolutePath().endsWith("internal")
                    || file.getAbsolutePath().endsWith("dbase")
                    || file.getAbsolutePath().endsWith("dbase_new"))) {
                fail("Unexpected folder: " + file.getAbsolutePath());
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
        assertThat(subject.isExportDir(folder.toFile()), is(true));
    }

    @Test
    public void isExportDir_created() {
        subject.createExportFileWriters(folder.toFile(), LAST_SERIAL);
        assertThat(subject.isExportDir(folder.toFile()), is(true));
    }

    @Test
    public void isLastSerialFile_created() throws IOException {
        subject.createExportFileWriters(folder.toFile(), LAST_SERIAL);

        final File currentSerialFile = new File(folder.toFile(), "dbase/RIPE.CURRENTSERIAL");
        assertThat(currentSerialFile.exists(), is(true));

        final String savedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(savedSerial, is(String.valueOf(LAST_SERIAL)));

        final File newSerialFile = new File(folder.toFile(), "dbase_new/RIPE.CURRENTSERIAL");
        assertThat(newSerialFile.exists(), is(true));

        final String newSavedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(newSavedSerial, is(String.valueOf(LAST_SERIAL)));
    }
}
