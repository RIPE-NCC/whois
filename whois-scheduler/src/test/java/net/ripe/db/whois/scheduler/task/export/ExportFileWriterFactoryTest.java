package net.ripe.db.whois.scheduler.task.export;

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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ExportFileWriterFactoryTest {
    private static final int LAST_SERIAL = 1234;

    @TempDir
    public Path folder;

    @Mock
    DummifierNrtm dummifierNrtm;
    ExportFileWriterFactory subject;

    @BeforeEach
    public void setup() {
        subject = new ExportFileWriterFactory(dummifierNrtm, "internal", "public", "TEST", "TEST-NONAUTH");
    }

    @Test
    public void createExportFileWriters_existing_dir() throws IOException {
        Files.createDirectories(folder.resolve("public"));

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
        assertThat(files.length, is(2));

        assertThat(Arrays.stream(files).map(File::getAbsolutePath).toList(), containsInAnyOrder(endsWith("internal"), endsWith("public")));
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

        final File currentSerialFile = new File(folder.toFile(), "public/TEST.CURRENTSERIAL");
        assertThat(currentSerialFile.exists(), is(true));

        final String savedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(savedSerial, is(String.valueOf(LAST_SERIAL)));

        final String newSavedSerial = new String(FileCopyUtils.copyToByteArray(currentSerialFile), StandardCharsets.ISO_8859_1);
        assertThat(newSavedSerial, is(String.valueOf(LAST_SERIAL)));
    }
}
