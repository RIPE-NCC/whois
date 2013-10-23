package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.LoggedUpdateProcessor;
import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyLogFolder extends LogSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyLogFolder.class);

    public static final Pattern DAILY_LOG_FOLDER_PATTERN = Pattern.compile("(?:^|.*/)(\\d{8})$");
    public static final Pattern UPDATE_LOG_FOLDER_PATTERN = Pattern.compile("(?:^|.*/)(\\d{6})\\..*");

    private final Path dailyLogFolder;
    private final String path;
    private final String date;

    public DailyLogFolder(final Path dailyLogFolder) {
        final Matcher folderNameMatcher = DAILY_LOG_FOLDER_PATTERN.matcher(dailyLogFolder.toString());
        if (!folderNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid dailyLogFolder: " + dailyLogFolder + " (should match pattern '" + DAILY_LOG_FOLDER_PATTERN.pattern() + "')");
        }

        this.date = folderNameMatcher.group(1);

        if (!Files.exists(dailyLogFolder) || !Files.isDirectory(dailyLogFolder)) {
            throw new IllegalArgumentException("Folder " + dailyLogFolder + " does not exist");
        }

        this.dailyLogFolder = dailyLogFolder;
        this.path = dailyLogFolder.toAbsolutePath().toString();
    }

    public void processLoggedFiles(final LoggedUpdateProcessor loggedUpdateProcessor) {
        try (final DirectoryStream<Path> updateLogFolders = Files.newDirectoryStream(dailyLogFolder, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                if (Files.isDirectory(entry) && UPDATE_LOG_FOLDER_PATTERN.matcher(entry.toString()).matches()) {
                    return true;
                }
                return false;
            }
        })) {

            for (final Path updateLogFolder : updateLogFolders) {
                try (final DirectoryStream<Path> updateLogEntries = Files.newDirectoryStream(updateLogFolder, new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return Files.isRegularFile(entry) && NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher(entry.toString()).matches();
                    }
                })) {

                    for (final Path updateLogEntry : updateLogEntries) {
                        final DailyLogEntry dailyLogEntry = new DailyLogEntry(updateLogEntry.toAbsolutePath().toString(), date);

                        if (loggedUpdateProcessor.accept(dailyLogEntry)) {
                            try (InputStream is = Files.newInputStream(updateLogEntry)) {
                                loggedUpdateProcessor.process(dailyLogEntry, getGzippedContent(is, Files.size(updateLogEntry)));
                            } catch (IOException e) {
                                LOGGER.warn("IO exception processing file: {}", updateLogEntry, e);
                            } catch (RuntimeException e) {
                                LOGGER.warn("Unexpected exception processing file: {}", updateLogEntry, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
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
}
