package net.ripe.db.whois.internal.logsearch.logformat;

import net.ripe.db.whois.internal.logsearch.LoggedUpdateProcessor;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyLogFile extends LogSource {

    public static final Pattern LOGFILE_PATTERN = Pattern.compile("(?:^|.*/)(updlog|acklog)\\.(\\d{8})\\.bz2$");
    public static final Pattern LOGSECTION_PATTERN = Pattern.compile(">>> [t|T]ime: .*(\\d\\d:\\d\\d:\\d\\d) .*(\\w{4} UPDATE) (ACK)?\\s?(\\(.*\\))?\\s?<<<");

    private final String path;
    private final String date;

    public LegacyLogFile(final String path) {
        final Matcher matcher = LOGFILE_PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid legacyLogFile: " + path + " (should match pattern '" + LOGFILE_PATTERN.pattern() + "')");
        }

        this.date = matcher.group(2);
        this.path = path;
    }

    public void processLogFile(final LoggedUpdateProcessor processor) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(path))))) {
            createLegacyLog(date, bufferedReader, processor);
        } catch (IOException ignore) {}
    }

    private void createLegacyLog(final String date, final BufferedReader reader, final LoggedUpdateProcessor processor) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        int count = 0;

        while ((line = reader.readLine()) != null) {
            final LegacyLogEntry logEntry = new LegacyLogEntry(path + File.separator + count, date);
            if (LOGSECTION_PATTERN.matcher(line).matches() && processor.accept(logEntry)) {
                processor.process(logEntry, builder.toString());
                count++;

                builder = new StringBuilder();
            }
            builder.append(line).append('\n');
        }

        final LegacyLogEntry logEntry = new LegacyLogEntry(path + File.separator + count, date);
        if (!StringUtils.isBlank(builder.toString()) && processor.accept(logEntry)) {
            processor.process(logEntry, builder.toString());
        }
    }

    public String getDate() {
        return date;
    }
}
