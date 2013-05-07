package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
class ExportFileWriterFactory {
    private static final String EXTERNAL_EXPORT_FOLDERNAME = "dbase";
    private static final String INTERNAL_EXPORT_FOLDERNAME = "internal";
    private static final String SPLITFILE_FOLDERNAME = "split";
    private static final String CURRENTSERIAL_FILENAME = "RIPE.CURRENTSERIAL";

    private final Dummifier dummifier;

    @Autowired
    ExportFileWriterFactory(final Dummifier dummifier) {
        this.dummifier = dummifier;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDir = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME);
        final File splitDir = new File(baseDir, EXTERNAL_EXPORT_FOLDERNAME + File.separator + SPLITFILE_FOLDERNAME);
        final File splitUnmodifiedDir = new File(baseDir, INTERNAL_EXPORT_FOLDERNAME + File.separator + SPLITFILE_FOLDERNAME);

        initDirs(fullDir, splitDir, splitUnmodifiedDir);

        try {
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.ISO_8859_1), new File(fullDir, CURRENTSERIAL_FILENAME));
        } catch (IOException e) {
            throw new RuntimeException("Writing current serial", e);
        }

        return Lists.newArrayList(
                new ExportFileWriter(fullDir, new FilenameStrategy.SingleFile(), new DecorationStrategy.Dummify(dummifier)),
                new ExportFileWriter(splitDir, new FilenameStrategy.SplitFile(), new DecorationStrategy.Dummify(dummifier)),
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
            if (!fileName.equals(EXTERNAL_EXPORT_FOLDERNAME) && !fileName.equals(INTERNAL_EXPORT_FOLDERNAME)) {
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
