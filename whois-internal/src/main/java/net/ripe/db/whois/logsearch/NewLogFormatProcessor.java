package net.ripe.db.whois.logsearch;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.logsearch.logformat.DailyLogEntry;
import net.ripe.db.whois.logsearch.logformat.DailyLogFolder;
import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;
import net.ripe.db.whois.logsearch.logformat.TarredLogEntry;
import net.ripe.db.whois.logsearch.logformat.TarredLogFile;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class NewLogFormatProcessor implements LogFormatProcessor {
    public static final Pattern INDEXED_LOG_ENTRIES = Pattern.compile("(?:^|.*/)[0-9]+\\.(?:msg-in|msg-out)\\.txt\\.gz$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NewLogFormatProcessor.class);

    private final LogFileIndex logFileIndex;
    private final String logDirectory;

    @Autowired
    public NewLogFormatProcessor(
            @Value("${dir.update.audit.log}") final String logDirectory,
            final LogFileIndex logFileIndex) {
        this.logDirectory = logDirectory;
        this.logFileIndex = logFileIndex;
    }

    @PostConstruct
    private void init() {
        final File file = new File(logDirectory);
        if (file.exists()) {
            LOGGER.info("Using log dir: {}", file.getAbsolutePath());
        } else if (file.mkdirs()) {
            LOGGER.warn("Created log dir: {}", file.getAbsolutePath());
        } else {
            throw new IllegalArgumentException(String.format("Unable to create log dir: %s", file.getAbsolutePath()));
        }
    }

    public void addTarFileToIndex(final String path) throws IOException {
        final TarredLogFile tarredLogFile = new TarredLogFile(new File(path));
        logFileIndex.update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                tarredLogFile.processLoggedFiles(new LoggedUpdateProcessor<TarredLogEntry>() {
                    @Override
                    public boolean accept(final TarredLogEntry tarredLogEntry) {
                        return INDEXED_LOG_ENTRIES.matcher(tarredLogEntry.getUpdateId()).matches();
                    }

                    @Override
                    public void process(final TarredLogEntry tarredLogEntry, final String contents) {
                        LogFileIndex.addToIndex(tarredLogEntry, contents, indexWriter);
                    }
                });
            }
        });
    }

    public void addDailyLogFolderToIndex(final String path) throws IOException {
        logFileIndex.update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                new DailyLogFolder(Paths.get(path)).processLoggedFiles(new LoggedUpdateProcessor<DailyLogEntry>() {
                    @Override
                    public boolean accept(DailyLogEntry dailyLogEntry) {
                        return INDEXED_LOG_ENTRIES.matcher(dailyLogEntry.getUpdateId()).matches();
                    }

                    @Override
                    public void process(DailyLogEntry dailyLogEntry, String contents) {
                        LogFileIndex.addToIndex(dailyLogEntry, contents, indexWriter);
                    }
                });
            }
        });
    }

    @Override
    public void addFileToIndex(final String path) throws IOException {
        if (DailyLogFolder.DAILY_LOG_FOLDER_PATTERN.matcher(path).matches()) {
            addDailyLogFolderToIndex(path);
        } else if (TarredLogFile.LOGFILE_PATTERN.matcher(path).matches()) {
            addTarFileToIndex(path);
        }
    }

    @Override
    public void addDirectoryToIndex(final String folder) throws IOException {
        try {
            Files.walkFileTree(FileSystems.getDefault().getPath(folder), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    final String absolutePath = dir.toAbsolutePath().toString();
                    if (DailyLogFolder.DAILY_LOG_FOLDER_PATTERN.matcher(absolutePath).matches()) {
                        addDailyLogFolderToIndex(absolutePath);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    final String absolutePath = file.toAbsolutePath().toString();
                    if (TarredLogFile.LOGFILE_PATTERN.matcher(absolutePath).matches()) {
                        addTarFileToIndex(absolutePath);
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

    @Scheduled(fixedDelay = 60 * 1000)
    public void incrementalUpdate() {
        logFileIndex.update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                final Set<String> todaysIndexedFiles = Sets.newHashSet(Iterables.transform(logFileIndex.searchByDate(LocalDate.now()), new Function<LoggedUpdate, String>() {
                    @Override
                    public String apply(final LoggedUpdate input) {
                        return input.getUpdateId();
                    }
                }));

                final String todaysFolder = LogFileIndex.DATE_FORMATTER.print(LocalDate.now());

                try {
                    Files.walkFileTree(Paths.get(logDirectory), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                            if (dir.getFileName().toString().equals(todaysFolder)) {
                                new DailyLogFolder(dir).processLoggedFiles(new LoggedUpdateProcessor<DailyLogEntry>() {
                                    @Override
                                    public boolean accept(DailyLogEntry dailyLogEntry) {
                                        return INDEXED_LOG_ENTRIES.matcher(dailyLogEntry.getUpdateId()).matches() &&
                                                !todaysIndexedFiles.contains(dailyLogEntry.getUpdateId());
                                    }

                                    @Override
                                    public void process(DailyLogEntry dailyLogEntry, String contents) {
                                        LogFileIndex.addToIndex(dailyLogEntry, contents, indexWriter);
                                    }
                                });

                                return FileVisitResult.SKIP_SUBTREE;
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
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void dailyUpdate() {
        for (int i = 3; i > 1; i--) {
            try {
                final LocalDate prevDate = LocalDate.now().minusDays(i);
                final String prevDayFolder = LogFileIndex.DATE_FORMATTER.print(prevDate);
                final String prevDayTar = prevDayFolder + ".tar";

                logFileIndex.removeAllByDate(prevDate);

                Files.walkFileTree(Paths.get(logDirectory), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                        if (dir.getFileName().toString().equals(prevDayFolder)) {
                            addDailyLogFolderToIndex(dir.toAbsolutePath().toString());
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().equals(prevDayTar)) {
                            if (System.currentTimeMillis() - Files.getLastModifiedTime(file).toMillis() < 30 * 60 * 1000) {
                                throw new IllegalArgumentException("TAR file " + file + " was last modified with 30 minutes; skipping full reindex of " + prevDayTar);
                            }
                            addTarFileToIndex(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e) {
                        LOGGER.info("Visit file: {} failed: {}", file.toAbsolutePath(), e.getMessage());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IllegalArgumentException | IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
