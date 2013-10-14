package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.task.export.dao.ExportCallbackHandler;
import net.ripe.db.whois.scheduler.task.export.dao.ExportDao;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
class RpslObjectsToTextExporter implements RpslObjectsExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectsToTextExporter.class);

    private final ExportFileWriterFactory exportFileWriterFactory;
    private final ExportDao exportDao;
    private final TagsDao tagsDao;
    private final File exportDir;
    private final File tmpDir;

    private final AtomicBoolean exporting = new AtomicBoolean();
    private final boolean enabled;

    @Autowired
    public RpslObjectsToTextExporter(final ExportFileWriterFactory exportFileWriterFactory,
                                     final ExportDao exportDao,
                                     final TagsDao tagsDao,
                                     @Value("${dir.rpsl.export}") final String exportDirName,
                                     @Value("${dir.rpsl.export.tmp}") final String tmpDirName,
                                     @Value("${rpsl.export.enabled:true}") final boolean enabled) {
        this.exportFileWriterFactory = exportFileWriterFactory;
        this.exportDao = exportDao;
        this.tagsDao = tagsDao;
        this.enabled = enabled;

        exportDir = new File(exportDirName);
        tmpDir = new File(tmpDirName);

        initDirs();
    }

    public void export() {
        if (!enabled) {
            return;
        }
        if (exporting.getAndSet(true)) {
            throw new IllegalStateException("Export already in progress");
        }

        try {
            LOGGER.info("Database export started");
            final Stopwatch stopwatch = new Stopwatch().start();

            initDirs();
            exportToFiles();

            Validate.isTrue(FileSystemUtils.deleteRecursively(exportDir), "Recursive delete failed: ", exportDir);
            Validate.isTrue(tmpDir.renameTo(exportDir), "Rename failed: ", tmpDir);

            LOGGER.info("Database export complete after {} in {}", stopwatch.stop().toString(), exportDir);
        } finally {
            exporting.set(false);
        }
    }

    private void initDirs() {
        if (exportDir.exists()) {
            Validate.isTrue(exportDir.canWrite(), "Unable to write to: ", exportDir);
            Validate.isTrue(exportFileWriterFactory.isExportDir(exportDir), "Existing dir does not have correct structure: ", exportDir);
            LOGGER.info("Use existing: {}", exportDir);
        } else {
            Validate.isTrue(exportDir.mkdirs(), "Unable to create export dir: ", exportDir);
            LOGGER.info("Created: {}", exportDir);
        }

        if (tmpDir.exists()) {
            Validate.isTrue(exportFileWriterFactory.isExportDir(tmpDir), "Existing dir does not have correct structure: ", tmpDir);
            Validate.isTrue(FileSystemUtils.deleteRecursively(tmpDir), "Unable to remove temp dir: ", tmpDir);
            LOGGER.info("Delete existing: {}", tmpDir);
        }

        Validate.isTrue(tmpDir.mkdirs(), "Unable to create temp dir: ", tmpDir);
        LOGGER.info("Created: {}", tmpDir);
    }

    private void exportToFiles() {
        // TODO: [AH] this should be in a single transaction, or the max. serial could have changed while exporting data
        final int maxSerial = exportDao.getMaxSerial();
        LOGGER.info("Max serial: {}", maxSerial);

        final List<ExportFileWriter> exportFileWriters = exportFileWriterFactory.createExportFileWriters(tmpDir, maxSerial);
        try {
            final TextFileExporter textFileExporter = new TextFileExporter(exportFileWriters);
            try {
                exportDao.exportObjects(textFileExporter);
            } finally {
                textFileExporter.logNrExported();
            }
        } finally {
            for (final ExportFileWriter exportFileWriter : exportFileWriters) {
                exportFileWriter.close();
            }
        }
    }

    private final class TextFileExporter implements ExportCallbackHandler {
        private static final int LOG_EVERY = 500000;
        private final Iterable<ExportFileWriter> exportFileWriters;

        private int lastLogged = -1;
        private int nrExported = 0;

        private TextFileExporter(final Iterable<ExportFileWriter> exportFileWriters) {
            this.exportFileWriters = exportFileWriters;
        }

        @Override
        public void exportObject(final RpslObject object) {
            final List<Tag> tags = tagsDao.getTags(object.getObjectId());
            for (final ExportFileWriter exportFileWriter : exportFileWriters) {
                try {
                    exportFileWriter.write(object, tags);
                } catch (IOException e) {
                    throw new RuntimeException("Exporting to " + exportFileWriter, e);
                }
            }

            if (++nrExported % LOG_EVERY == 0) {
                logNrExported();
            }
        }

        public void logNrExported() {
            if (lastLogged != nrExported) {
                LOGGER.info("Exported {} objects", nrExported);
                lastLogged = nrExported;
            }
        }
    }
}
