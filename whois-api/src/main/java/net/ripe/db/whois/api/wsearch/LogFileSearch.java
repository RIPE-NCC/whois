package net.ripe.db.whois.api.wsearch;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class LogFileSearch {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?im)^((?:password|override):\\s*)(.*)\\s?");

    private final File logDir;
    private final LogFileIndex logFileIndex;

    @Autowired
    public LogFileSearch(
            @Value("${dir.update.audit.log}") final String logDir,
            final LogFileIndex logFileIndex) {
        this.logDir = new File(logDir);
        this.logFileIndex = logFileIndex;
    }

    public Set<LoggedUpdateId> searchLoggedUpdateIds(final String queryString, final LocalDate date) throws IOException, ParseException {
        return logFileIndex.searchLoggedUpdateIds(queryString, date);
    }

    public void writeLoggedUpdates(final LoggedUpdateId loggedUpdateId, final Writer writer) throws IOException {
        new DailyLogFolder(new File(logDir, loggedUpdateId.getDailyLogFolder())).processLoggedFiles(new DailyLogFolder.LoggedFilesProcessor() {
            @Override
            public boolean accept(final LoggedUpdateInfo loggedUpdateInfo) {
                return loggedUpdateInfo.getLoggedUpdateId().equals(loggedUpdateId) && !loggedUpdateInfo.getType().equals(LoggedUpdateInfo.Type.AUDIT);
            }

            @Override
            public void process(final LoggedUpdateInfo loggedUpdateInfo, final String contents) {
                final String filteredContents = filterContents(loggedUpdateInfo, contents);

                try {
                    final String box = StringUtils.repeat("#", 115);

                    writer.write(box);
                    writer.write("\n");
                    writer.write(format("date", loggedUpdateInfo.getLoggedUpdateId().getDailyLogFolder()));
                    writer.write(format("folder", loggedUpdateInfo.getLoggedUpdateId().getUpdateFolder()));
                    writer.write(format("type", loggedUpdateInfo.getType().name()));
                    writer.write(format("filename", loggedUpdateInfo.getFilename()));
                    writer.write(box);
                    writer.write("\n\n");
                    writer.write(filteredContents);
                    writer.write("\n\n");
                } catch (IOException e) {
                    throw new IllegalStateException("Writing contents", e);
                }
            }

            private String format(final String key, final String value) throws IOException {
                return String.format("# %-9s: %-100s #\n", key, value);
            }
        });
    }

    private String filterContents(final LoggedUpdateInfo loggedUpdateInfo, final String contents) {
        final LoggedUpdateInfo.Type type = loggedUpdateInfo.getType();
        if (!type.equals(LoggedUpdateInfo.Type.AUDIT) && !type.equals(LoggedUpdateInfo.Type.UPDATE)) {
            return contents;
        }

        return PASSWORD_PATTERN.matcher(contents).replaceAll("$1FILTERED\n");
    }
}
