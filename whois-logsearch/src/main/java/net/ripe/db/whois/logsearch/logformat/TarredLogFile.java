package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.LoggedUpdateProcessor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TarredLogFile extends LogSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TarredLogFile.class);
    public static final Pattern LOGFILE_PATTERN = Pattern.compile("(?:^|.*/)(\\d{8})(\\.tar)");

    private final String path;
    private final String date;

    public TarredLogFile(final File tarFile) {
        final Matcher folderNameMatcher = LOGFILE_PATTERN.matcher(tarFile.getName());
        if (!folderNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid tarfile: " + tarFile.getName() + " (should match pattern '" + LOGFILE_PATTERN.pattern() + "')");
        }

        this.date = folderNameMatcher.group(1);

        if (!tarFile.exists() || !tarFile.isFile()) {
            throw new IllegalArgumentException("Folder " + tarFile.getAbsolutePath() + " does not exist");
        }

        this.path = tarFile.getAbsolutePath();
    }

    public void processLoggedFiles(final LoggedUpdateProcessor loggedUpdateProcessor) {
        try (final TarArchiveInputStream tarInput = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(path)))) {

            for (TarArchiveEntry tarEntry = tarInput.getNextTarEntry(); tarEntry != null; tarEntry = tarInput.getNextTarEntry()) {
                String tarEntryName = tarEntry.getName();
                if (tarEntryName.startsWith("./")) {
                    tarEntryName = tarEntryName.substring(2);
                }

                try {
                    if (tarEntry.isFile()) {
                        try {
                            final LoggedUpdate loggedUpdate = new TarredLogEntry(path, date, tarEntryName);
                            if (loggedUpdateProcessor.accept(loggedUpdate)) {
                                loggedUpdateProcessor.process(loggedUpdate, getGzippedContent(tarInput, tarEntry.getSize()));
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.debug("Ignoring {}", tarEntryName);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("IO exception processing entry: {} in file: {}", tarEntryName, path, e);
                } catch (RuntimeException e) {
                    LOGGER.warn("Unexpected exception processing entry: {} in file: {}", tarEntryName, path, e);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Exception processing tarfile: {}", path, e);
        }
    }

    public String getDate() {
        return this.date;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TarredLogFile that = (TarredLogFile) o;
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
