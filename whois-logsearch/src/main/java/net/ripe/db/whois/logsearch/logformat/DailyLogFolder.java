package net.ripe.db.whois.logsearch.logformat;

import com.google.common.collect.Maps;
import net.ripe.db.whois.logsearch.LoggedUpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyLogFolder extends LogSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyLogFolder.class);

    public static final Pattern DAILY_LOG_FOLDER_PATTERN = Pattern.compile("(?:^|.*/)(\\d{8})$");
    public static final Pattern UPDATE_LOG_FOLDER_PATTERN = Pattern.compile("(?:^|.*/)(\\d{6})\\..*");

    private final File dailyLogFolder;
    private final String path;
    private final String date;

    public DailyLogFolder(final File dailyLogFolder) {
        final Matcher folderNameMatcher = DAILY_LOG_FOLDER_PATTERN.matcher(dailyLogFolder.getName());
        if (!folderNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid dailyLogFolder: " + dailyLogFolder.getName() + " (should match pattern '" + DAILY_LOG_FOLDER_PATTERN.pattern() + "')");
        }

        this.date = folderNameMatcher.group(1);

        if (!dailyLogFolder.exists() || !dailyLogFolder.isDirectory()) {
            throw new IllegalArgumentException("Folder " + dailyLogFolder.getAbsolutePath() + " does not exist");
        }

        this.dailyLogFolder = dailyLogFolder;
        this.path = dailyLogFolder.getAbsolutePath();
    }

    public void processLoggedFiles(final LoggedUpdateProcessor loggedUpdateProcessor) {
        final File[] updateLogFolders = dailyLogFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathName) {
                return pathName.isDirectory() && UPDATE_LOG_FOLDER_PATTERN.matcher(pathName.getName()).matches();
            }
        });

        final SortedMap<LoggedUpdate, File> loggedUpdates = Maps.newTreeMap();

        for (final File updateLogFolder : updateLogFolders) {
            final File[] files = updateLogFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathName) {
                    return pathName.isFile();
                }
            });

            for (final File file : files) {
                try {
                    loggedUpdates.put(new DailyLogEntry(file.getAbsolutePath(), date), file);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Ignoring {}", file.getAbsolutePath());
                }
            }
        }

        for (final Map.Entry<LoggedUpdate, File> updateInfoFileEntry : loggedUpdates.entrySet()) {
            final LoggedUpdate loggedUpdate = updateInfoFileEntry.getKey();
            final File file = updateInfoFileEntry.getValue();

            if (loggedUpdateProcessor.accept(loggedUpdate)) {
                try (InputStream is = new FileInputStream(file)) {
                    loggedUpdateProcessor.process(loggedUpdate, getGzippedContent(is, file.length()));
                } catch (IOException e) {
                    LOGGER.warn("IO exception processing file: {}", file.getAbsolutePath(), e);
                } catch (RuntimeException e) {
                    LOGGER.warn("Unexpected exception processing file: {}", file.getAbsolutePath(), e);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DailyLogFolder that = (DailyLogFolder) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path;
    }

    public static List<DailyLogFolder> getDailyLogFolders(final Path path, final long lastUpdatedFrom, final long lastUpdatedTo) {
        final List<DailyLogFolder> dailyLogFolders = new ArrayList<DailyLogFolder>();

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (DAILY_LOG_FOLDER_PATTERN.matcher(dir.toString()).matches()) {
                        final long fileLastMofified = attrs.lastModifiedTime().toMillis();
                        if (fileLastMofified >= lastUpdatedFrom && fileLastMofified < lastUpdatedTo) {
                            dailyLogFolders.add(new DailyLogFolder(dir.toFile()));
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return dailyLogFolders;
    }
}
