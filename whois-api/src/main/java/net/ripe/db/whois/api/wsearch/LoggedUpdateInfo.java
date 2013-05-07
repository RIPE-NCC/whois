package net.ripe.db.whois.api.wsearch;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
class LoggedUpdateInfo implements Comparable<LoggedUpdateInfo> {
    private static final Pattern UPDATE_LOG_FILE_PATTERN = Pattern.compile("(?i)(\\d+).*\\.gz");

    static enum Type {AUDIT, UPDATE, ACK, EMAIL}

    private final LoggedUpdateId loggedUpdateId;
    private final String filename;
    private final Type type;

    private LoggedUpdateInfo(final LoggedUpdateId loggedUpdateId, final String filename, final Type type) {
        this.loggedUpdateId = loggedUpdateId;
        this.filename = filename;
        this.type = type;
    }

    public LoggedUpdateId getLoggedUpdateId() {
        return loggedUpdateId;
    }

    public String getFilename() {
        return filename;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%-12s] %s/%s", type.name(), loggedUpdateId, filename);
    }

    private static Type getType(final String filename) {
        final Matcher matcher = UPDATE_LOG_FILE_PATTERN.matcher(filename);
        Validate.isTrue(matcher.matches());

        final String id = matcher.group(1);
        if (id.equals("000")) {
            return Type.AUDIT;
        } else if (id.equals("001")) {
            return Type.UPDATE;
        } else if (id.equals("002")) {
            return Type.ACK;
        } else {
            return Type.EMAIL;
        }
    }

    static LoggedUpdateInfo parse(final String filePath) {
        final int filenameSeparator = filePath.lastIndexOf(File.separatorChar);
        if (filenameSeparator == -1) {
            throw new IllegalArgumentException(String.format("Invalid path: %s", filePath));
        }

        final LoggedUpdateId loggedUpdateId = LoggedUpdateId.parse(filePath.substring(0, filenameSeparator));
        final String filename = filePath.substring(filenameSeparator + 1);
        final Type type = getType(filename);

        return new LoggedUpdateInfo(loggedUpdateId, filename, type);
    }

    static boolean isLoggedUpdateInfo(final String filename) {
        return UPDATE_LOG_FILE_PATTERN.matcher(filename).find();
    }

    @Override
    public int compareTo(final LoggedUpdateInfo other) {
        return new CompareToBuilder()
                .append(loggedUpdateId, other.loggedUpdateId)
                .append(type, other.type)
                .append(filename, other.filename)
                .toComparison();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LoggedUpdateInfo that = (LoggedUpdateInfo) o;
        return loggedUpdateId.equals(that.loggedUpdateId) && type == that.type && filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
        int result = loggedUpdateId.hashCode();
        result = 31 * result + filename.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
