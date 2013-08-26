package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
class ExportFileWriterFactory {
    private static final String SPLITFILE_FOLDERNAME = "split";
    private static final String CURRENTSERIAL_FILENAME = "RIPE.CURRENTSERIAL";

    private final DummifierLegacy dummifierLegacy;
    private final DummifierCurrent dummifierCurrent;

    private final String legacyExternalExportDir;
    private final String externalExportDir;
    private final String internalExportDir;

    @Autowired
    ExportFileWriterFactory(final DummifierLegacy dummifierLegacy, final DummifierCurrent dummifierCurrent,
                            @Value("${dir.rpsl.export.internal}") String internalExportDir,
                            @Value("${dir.rpsl.export.external}") String externalExportDir,
                            @Value("${dir.rpsl.export.external.legacy}") String legacyExternalExportDir) {
        this.dummifierLegacy = dummifierLegacy;
        this.dummifierCurrent = dummifierCurrent;
        this.internalExportDir = internalExportDir;
        this.externalExportDir = externalExportDir;
        this.legacyExternalExportDir = legacyExternalExportDir;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDir = new File(baseDir, legacyExternalExportDir);
        final File fullDirNew = new File(baseDir, externalExportDir);
        final File splitDir = new File(baseDir, legacyExternalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File splitDirNew = new File(baseDir, externalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File internalDir = new File(baseDir, internalExportDir + File.separator + SPLITFILE_FOLDERNAME);

        initDirs(fullDirNew, fullDir, splitDirNew, splitDir, internalDir);

        try {
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.UTF_8), new File(fullDirNew, CURRENTSERIAL_FILENAME));
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.UTF_8), new File(fullDir, CURRENTSERIAL_FILENAME));
        } catch (IOException e) {
            throw new RuntimeException("Writing current serial", e);
        }

        return Lists.newArrayList(
                new ExportFileWriter(fullDir, new FilenameStrategy.SingleFile(), new DecorationStrategy.DummifyLegacy(dummifierLegacy)),
                new ExportFileWriter(splitDir, new FilenameStrategy.SplitFile(), new DecorationStrategy.DummifyLegacy(dummifierLegacy)),
                new ExportFileWriter(fullDirNew, new FilenameStrategy.SingleFile(), new DecorationStrategy.DummifyCurrent(dummifierCurrent)),
                new ExportFileWriter(splitDirNew, new FilenameStrategy.SplitFile(), new DecorationStrategy.DummifyCurrent(dummifierCurrent)),
                new ExportFileWriter(internalDir, new FilenameStrategy.SplitFile(), new DecorationStrategy.None())
        );
    }

    public boolean isExportDir(final File dir) {
        final File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }

        for (final File file : files) {
            if (file.isFile()) {
                return false;
            }

            final String fileName = file.getName();
            if (! (fileName.equals(externalExportDir)
                    || fileName.equals(internalExportDir)
                    || fileName.equals(legacyExternalExportDir))) {
                return false;
            }
        }

        return true;
    }

    private void initDirs(final File... dirs) {
        for (final File dir : dirs) {
            if (dir.exists()) {
                throw new IllegalStateException("Directory already exists: " + dir.getAbsolutePath());
            }

            if (!dir.mkdirs()) {
                throw new IllegalArgumentException("Unable to create directory: " + dir.getAbsolutePath());
            }
        }
    }
}
