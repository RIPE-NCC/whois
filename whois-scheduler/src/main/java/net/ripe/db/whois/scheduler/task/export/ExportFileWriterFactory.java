package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
class ExportFileWriterFactory {
    private static final String EXTERNAL_EXPORT_FOLDERNAME_OLD = "dbase_old";
    private static final String EXTERNAL_EXPORT_FOLDERNAME = "dbase";
    private static final String INTERNAL_EXPORT_FOLDERNAME = "internal";
    private static final String SPLITFILE_FOLDERNAME = "split";
    private static final String CURRENTSERIAL_FILENAME = "RIPE.CURRENTSERIAL";

    private final DummifierLegacy dummifierLegacy;
    private final DummifierCurrent dummifierCurrent;

    @Autowired
    ExportFileWriterFactory(final DummifierLegacy dummifierLegacy, final DummifierCurrent dummifierCurrent) {
        this.dummifierLegacy = dummifierLegacy;
        this.dummifierCurrent = dummifierCurrent;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDirOld = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME_OLD);
        final File fullDir = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME);
        final File splitDirOld = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME_OLD + File.separator + SPLITFILE_FOLDERNAME);
        final File splitDir = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME + File.separator + SPLITFILE_FOLDERNAME);
        final File splitUnmodifiedDir = new File(baseDir, INTERNAL_EXPORT_FOLDERNAME + File.separator + SPLITFILE_FOLDERNAME);

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
            if (! (fileName.equals(EXTERNAL_EXPORT_FOLDERNAME)
                    || fileName.equals(INTERNAL_EXPORT_FOLDERNAME)
                    || fileName.equals(EXTERNAL_EXPORT_FOLDERNAME_OLD))) {
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
