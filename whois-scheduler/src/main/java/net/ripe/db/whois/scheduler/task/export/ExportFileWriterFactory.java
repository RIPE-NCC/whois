package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
class ExportFileWriterFactory {
    private static final String SPLITFILE_FOLDERNAME = "split";
    private static final String CURRENTSERIAL_FILENAME = "RIPE.CURRENTSERIAL";

    private final DummifierNrtm dummifierNrtm;
    private final DummifierCurrent dummifierCurrent;

    private final String legacyExternalExportDir;
    private final String source;
    private final String nonAuthSource;
    private final String externalExportDir;
    private final String internalExportDir;

    @Autowired
    ExportFileWriterFactory(final DummifierNrtm dummifierNrtm, final DummifierCurrent dummifierCurrent,
                            @Value("${dir.rpsl.export.internal}") final String internalExportDir,
                            @Value("${dir.rpsl.export.external}") final String externalExportDir,
                            @Value("${dir.rpsl.export.external.legacy}") final String legacyExternalExportDir,
                            @Value("${whois.source}") final String source,
                            @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.dummifierNrtm = dummifierNrtm;
        this.dummifierCurrent = dummifierCurrent;
        this.internalExportDir = internalExportDir;
        this.externalExportDir = externalExportDir;
        this.legacyExternalExportDir = legacyExternalExportDir;
        this.source = source;
        this.nonAuthSource = nonAuthSource;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDir = new File(baseDir, legacyExternalExportDir);
        final File fullDirNew = new File(baseDir, externalExportDir);
        final File splitDir = new File(baseDir, legacyExternalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File splitDirNew = new File(baseDir, externalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File internalDir = new File(baseDir, internalExportDir + File.separator + SPLITFILE_FOLDERNAME);

        initDirs(fullDirNew, fullDir, splitDirNew, splitDir, internalDir);

        try {
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.ISO_8859_1), new File(fullDirNew, CURRENTSERIAL_FILENAME));
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(Charsets.ISO_8859_1), new File(fullDir, CURRENTSERIAL_FILENAME));
        } catch (IOException e) {
            throw new RuntimeException("Writing current serial", e);
        }

        final FilenameStrategy singleFile = new FilenameStrategy.SingleFile();
        final FilenameStrategy splitFile = new FilenameStrategy.SplitFile();

        final FilenameStrategy nonAuthSingleFile = new FilenameStrategy.NonAuthSingleFile();
        final FilenameStrategy nonAuthSplitFile = new FilenameStrategy.NonAuthSplitFile();

        final ExportFilter sourceFilter = new ExportFilter.SourceExportFilter(source, ImmutableSet.copyOf(ObjectType.values()));
        final ExportFilter nonAuthSourceFilter = new ExportFilter.SourceExportFilter(nonAuthSource, Sets.immutableEnumSet(AUT_NUM, ROUTE, ROUTE6), false);

        return Lists.newArrayList(
                new ExportFileWriter(fullDir, singleFile, new DecorationStrategy.DummifyLegacy(dummifierNrtm), sourceFilter),
                new ExportFileWriter(splitDir, splitFile, new DecorationStrategy.DummifyLegacy(dummifierNrtm), sourceFilter),
                new ExportFileWriter(fullDirNew, singleFile, new DecorationStrategy.DummifyCurrent(dummifierCurrent), sourceFilter),
                new ExportFileWriter(splitDirNew, splitFile, new DecorationStrategy.DummifyCurrent(dummifierCurrent), sourceFilter),
                new ExportFileWriter(internalDir, splitFile, new DecorationStrategy.None(), sourceFilter),

                new ExportFileWriter(fullDir, nonAuthSingleFile, new DecorationStrategy.DummifyLegacy(dummifierNrtm), nonAuthSourceFilter),
                new ExportFileWriter(splitDir, nonAuthSplitFile, new DecorationStrategy.DummifyLegacy(dummifierNrtm), nonAuthSourceFilter),
                new ExportFileWriter(fullDirNew, nonAuthSingleFile, new DecorationStrategy.DummifyCurrent(dummifierCurrent), nonAuthSourceFilter),
                new ExportFileWriter(splitDirNew, nonAuthSplitFile, new DecorationStrategy.DummifyCurrent(dummifierCurrent), nonAuthSourceFilter),
                new ExportFileWriter(internalDir, nonAuthSplitFile, new DecorationStrategy.None(), nonAuthSourceFilter)
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
