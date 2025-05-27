package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.AS_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
class ExportFileWriterFactory {

    protected static final Set<ObjectType> NONAUTH_OBJECT_TYPES = Sets.immutableEnumSet(AS_SET, AUT_NUM, ROUTE, ROUTE6);

    private static final String SPLITFILE_FOLDERNAME = "split";
    private static final String CURRENTSERIAL_SUFFIX = "CURRENTSERIAL";

    private final DummifierNrtm dummifierNrtm;
    private final String externalExportDir;
    private final String source;
    private final String nonAuthSource;
    private final String internalExportDir;

    @Autowired
    ExportFileWriterFactory(@Qualifier("dummifierNrtm") final DummifierNrtm dummifierNrtm,
                            @Value("${dir.rpsl.export.internal}") final String internalExportDir,
                            @Value("${dir.rpsl.export.external}") final String externalExportDir,
                            @Value("${whois.source}") final String source,
                            @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.dummifierNrtm = dummifierNrtm;
        this.internalExportDir = internalExportDir;
        this.externalExportDir = externalExportDir;
        this.source = source;
        this.nonAuthSource = nonAuthSource;
    }

    public List<ExportFileWriter> createExportFileWriters(final File baseDir, final int lastSerial) {
        final File fullDir = new File(baseDir, externalExportDir);
        final File splitDir = new File(baseDir, externalExportDir + File.separator + SPLITFILE_FOLDERNAME);
        final File internalDir = new File(baseDir, internalExportDir + File.separator + SPLITFILE_FOLDERNAME);

        initDirs(fullDir, splitDir, internalDir);

        try {
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(StandardCharsets.ISO_8859_1), new File(fullDir, String.format("%s.%s", source, CURRENTSERIAL_SUFFIX)));
            FileCopyUtils.copy(String.valueOf(lastSerial).getBytes(StandardCharsets.ISO_8859_1), new File(fullDir, String.format("%s.%s", nonAuthSource, CURRENTSERIAL_SUFFIX)));
        } catch (IOException e) {
            throw new RuntimeException("Writing current serial", e);
        }

        final FilenameStrategy singleFile = new FilenameStrategy.SingleFile(source);
        final FilenameStrategy splitFile = new FilenameStrategy.SplitFile(source);

        final FilenameStrategy nonAuthSingleFile = new FilenameStrategy.NonAuthSingleFile(nonAuthSource);
        final FilenameStrategy nonAuthSplitFile = new FilenameStrategy.NonAuthSplitFile(nonAuthSource, NONAUTH_OBJECT_TYPES);

        final ExportFilter sourceFilter = new ExportFilter.SourceExportFilter(source, ImmutableSet.copyOf(ObjectType.values()));
        final ExportFilter nonAuthSourceFilter = new ExportFilter.SourceExportFilter(nonAuthSource, NONAUTH_OBJECT_TYPES, false);

        return Lists.newArrayList(
                new ExportFileWriter(fullDir, singleFile, new DecorationStrategy.DummifySplitFiles(dummifierNrtm), sourceFilter),
                new ExportFileWriter(splitDir, splitFile, new DecorationStrategy.DummifySplitFiles(dummifierNrtm), sourceFilter),
                new ExportFileWriter(internalDir, splitFile, new DecorationStrategy.None(), sourceFilter),

                new ExportFileWriter(fullDir, nonAuthSingleFile, new DecorationStrategy.DummifySplitFiles(dummifierNrtm), nonAuthSourceFilter),
                new ExportFileWriter(splitDir, nonAuthSplitFile, new DecorationStrategy.DummifySplitFiles(dummifierNrtm), nonAuthSourceFilter),
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
                    || fileName.equals(internalExportDir))) {
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
