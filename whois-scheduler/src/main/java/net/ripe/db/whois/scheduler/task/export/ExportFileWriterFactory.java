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

    private String legacyExternalExportDir;
    private String externalExportDir;
    private String internalExportDir;

    @Value("${dir.rpsl.export.internal:internal}")
    public void setInternalExportDir(String internalExportDir) {
        this.internalExportDir = internalExportDir;
    }

    @Value("${dir.rpsl.export.external:dbase_new}")
    public void setExternalExportDir(String externalExportDir) {
        this.externalExportDir = externalExportDir;
    }

    @Value("${dir.rpsl.export.internal:dbase}")
    public void setLegacyExternalExportDir(String legacyExternalExportDir) {
        this.legacyExternalExportDir = legacyExternalExportDir;
    }

    @Autowired
    ExportFileWriterFactory(final DummifierLegacy dummifierLegacy, final DummifierCurrent dummifierCurrent) {
        this.dummifierLegacy = dummifierLegacy;
        this.dummifierCurrent = dummifierCurrent;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDirOld = new File(baseDir, legacyExternalExportDir);
        final File fullDir = new File(baseDir, externalExportDir);
        final File splitDirOld = new File(baseDir, legacyExternalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File splitDir = new File(baseDir, externalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File splitUnmodifiedDir = new File(baseDir, internalExportDir + File.separator + SPLITFILE_FOLDERNAME);

        initDirs(fullDir, fullDirOld, splitDir, splitDirOld, splitUnmodifiedDir);

        try {
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.ISO_8859_1), new File(fullDir, CURRENTSERIAL_FILENAME));
        } catch (IOException e) {
            throw new RuntimeException("Writing current serial", e);
        }

        return Lists.newArrayList(
                new ExportFileWriter(fullDirOld, new FilenameStrategy.SingleFile(), new DecorationStrategy.DummifyLegacy(dummifierLegacy)),
                new ExportFileWriter(splitDirOld, new FilenameStrategy.SplitFile(), new DecorationStrategy.DummifyLegacy(dummifierLegacy)),
                new ExportFileWriter(fullDir, new FilenameStrategy.SingleFile(), new DecorationStrategy.DummifyCurrent(dummifierCurrent)),
                new ExportFileWriter(splitDir, new FilenameStrategy.SplitFile(), new DecorationStrategy.DummifyCurrent(dummifierCurrent)),
                new ExportFileWriter(splitUnmodifiedDir, new FilenameStrategy.SplitFile(), new DecorationStrategy.None())
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
