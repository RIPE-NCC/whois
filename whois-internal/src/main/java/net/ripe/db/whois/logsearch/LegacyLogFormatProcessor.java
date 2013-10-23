package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.logsearch.logformat.LegacyLogEntry;
import net.ripe.db.whois.logsearch.logformat.LegacyLogFile;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

// - legacy .bz2 (old format, dash separator between raw messages, 1 file/day/server)
@Component
public class LegacyLogFormatProcessor implements LogFormatProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyLogFormatProcessor.class);

    private final LogFileIndex logFileIndex;

    @Autowired
    public LegacyLogFormatProcessor(final LogFileIndex logFileIndex) {
        this.logFileIndex = logFileIndex;
    }

    @Override
    public void addFileToIndex(final String filePath) {
        addToIndex(new LegacyLogFile(filePath));
    }

    @Override
    public void addDirectoryToIndex(final String folder) {
        try {
            Files.walkFileTree(FileSystems.getDefault().getPath(folder), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final String absolutePath = file.toFile().getAbsolutePath();
                    if (LegacyLogFile.LOGFILE_PATTERN.matcher(absolutePath).matches()) {
                        addFileToIndex(absolutePath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    LOGGER.info("Visit file: {} failed: {}", file.toAbsolutePath(), e.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addToIndex(final LegacyLogFile legacyLogFile) {
        logFileIndex.update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                legacyLogFile.processLogFile(new LoggedUpdateProcessor<LegacyLogEntry>() {

                    @Override
                    public boolean accept(final LegacyLogEntry legacyLogEntry) {
                        return true;
                    }

                    @Override
                    public void process(final LegacyLogEntry legacyLogEntry, final String contents) {
                        LogFileIndex.addToIndex(legacyLogEntry, contents, indexWriter);
                    }
                });
            }

        });
    }
}
