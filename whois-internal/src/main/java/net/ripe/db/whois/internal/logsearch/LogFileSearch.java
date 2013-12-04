package net.ripe.db.whois.internal.logsearch;

import com.google.common.base.Splitter;
import net.ripe.db.whois.internal.logsearch.logformat.LegacyLogFile;
import net.ripe.db.whois.internal.logsearch.logformat.LogSource;
import net.ripe.db.whois.internal.logsearch.logformat.LoggedUpdate;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogFileSearch {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?im)^(override|password):\\s*(.+)\\s*$");
    private static final Splitter PATH_ELEMENT_SPLITTER = Splitter.on(File.separatorChar).omitEmptyStrings();

    private final LogFileIndex logFileIndex;

    @Autowired
    public LogFileSearch(final LogFileIndex logFileIndex) {
        this.logFileIndex = logFileIndex;
    }

    public Set<LoggedUpdate> searchLoggedUpdateIds(final String queryString, final LocalDate fromDate, final LocalDate toDate) throws IOException, ParseException {
        return logFileIndex.searchByDateRangeAndContent(queryString, fromDate, toDate);
    }

    public void writeLoggedUpdate(final LoggedUpdate loggedUpdate, final Writer writer) {
        final String filteredContents = filterContents(fetchContents(loggedUpdate));

        try {
            final String box = StringUtils.repeat("#", loggedUpdate.getUpdateId().length() + 15);

            writer.write(box);
            writer.write("\n");
            writer.write(String.format("# %-9s: %s%s #\n", "id", loggedUpdate.getUpdateId(), StringUtils.repeat(" ", box.length() - 15 - loggedUpdate.getUpdateId().length())));
            writer.write(String.format("# %-9s: %s%s #\n", "date", loggedUpdate.getDate(), StringUtils.repeat(" ", box.length() - 15 - loggedUpdate.getDate().length())));
            writer.write(box);
            writer.write("\n\n");
            writer.write(filteredContents);
            writer.write("\n\n");
        } catch (IOException ignored) {
            // don't care if clients disconnect
        }
    }

    private String fetchContents(LoggedUpdate loggedUpdate) {
        try {
            final Iterable<String> split = PATH_ELEMENT_SPLITTER.split(loggedUpdate.getUpdateId());
            return recurseIntoDir(new File("/"), split.iterator());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String recurseIntoDir(File dir, Iterator<String> path) {
        if (!path.hasNext()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + " is not an update log entry");
        }

        File file = new File(dir, path.next());

        if (file.isDirectory()) {
            return recurseIntoDir(file, path);
        } else if (file.isFile()) {
            return fetchFromArchive(file, path);
        } else {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is neither file nor directory");
        }
    }

    private String fetchFromArchive(File file, Iterator<String> path) {
        final String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".bz2")) {
            return fetchFromBzip2(file, path);
        } else if (fileName.endsWith(".tar")) {
            return fetchFromTar(file, path);
        } else if (fileName.endsWith(".gz")) {
            return fetchGzip(file);
        } else {
            throw new IllegalArgumentException("Unknown archive file: " + file.getAbsolutePath());
        }
    }

    private String fetchGzip(File file) {
        try (InputStreamReader reader = new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(file)))) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error processing gzip file: " + file.getAbsolutePath(), e);
        }
    }

    private String fetchFromTar(File file, Iterator<String> path) {
        final String lookup = StringUtils.join(path, '/');

        try (final TarArchiveInputStream tarInput = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            for (TarArchiveEntry tarEntry = tarInput.getNextTarEntry(); tarEntry != null; tarEntry = tarInput.getNextTarEntry()) {
                String tarEntryName = tarEntry.getName();
                if (tarEntryName.startsWith("./")) {
                    tarEntryName = tarEntryName.substring(2);
                }

                if (tarEntry.isFile() && tarEntryName.equals(lookup)) {
                    return LogSource.getGzippedContent(tarInput, tarEntry.getSize());
                }
            }
            throw new IllegalArgumentException(String.format("File: %s not found in tar archive: %s", lookup, file.getAbsolutePath()));

        } catch (IOException e) {
            throw new IllegalArgumentException("Error processing tar archive: " + file.getAbsolutePath(), e);
        }
    }

    private String fetchFromBzip2(File file, Iterator<String> path) {
        int count = Integer.parseInt(path.next());
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(file))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (LegacyLogFile.LOGSECTION_PATTERN.matcher(line).matches()) {
                    count--;
                    if (count < 0) {
                       return result.toString();
                    }
                }
                if (count==0){
                    result.append(line).append('\n');
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Error processing bzip2 archive: " + file.getAbsolutePath(), e);
        }

        return result.toString();
    }

    // we have to support historical override/password formats here, so we can't rely on whois-update for this functionality
    private String filterContents(final String contents) {
        final Matcher matcher = PASSWORD_PATTERN.matcher(contents);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(1).equalsIgnoreCase("password")) {
                matcher.appendReplacement(result, "password: FILTERED");
            } else {
                final String[] override = StringUtils.split(matcher.group(2), ',');
                if (override.length == 3) {
                    matcher.appendReplacement(result, String.format("override: %s, FILTERED, %s", override[0], override[2]));
                } else if (override.length == 2) {
                    matcher.appendReplacement(result, String.format("override: %s, FILTERED", override[0]));
                } else {
                    matcher.appendReplacement(result, "override: FILTERED");
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
