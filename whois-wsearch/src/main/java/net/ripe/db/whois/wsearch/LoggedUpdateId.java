package net.ripe.db.whois.wsearch;

import org.apache.commons.lang.builder.CompareToBuilder;

import java.io.File;

public class LoggedUpdateId implements Comparable<LoggedUpdateId> {
    private final String dailyLogFolder;
    private final String updateFolder;

    public LoggedUpdateId(final String dailyLogFolder, final String updateFolder) {
        this.dailyLogFolder = dailyLogFolder;
        this.updateFolder = updateFolder;
    }

    public String getDailyLogFolder() {
        return dailyLogFolder;
    }

    public String getUpdateFolder() {
        return updateFolder;
    }

    public static LoggedUpdateId parse(final String path) {
        try {
            final int updateFolderSeparator = path.lastIndexOf(File.separatorChar);
            final int dailyLogFolderSeparator = path.lastIndexOf(File.separatorChar, updateFolderSeparator - 1);

            final String dailyLogFolder = path.substring(dailyLogFolderSeparator + 1, updateFolderSeparator);
            final String updateFolder = path.substring(updateFolderSeparator + 1);

            return new LoggedUpdateId(dailyLogFolder, updateFolder);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("Invalid path: %s", path));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LoggedUpdateId that = (LoggedUpdateId) o;
        return !(!dailyLogFolder.equals(that.dailyLogFolder) || !updateFolder.equals(that.updateFolder));
    }

    @Override
    public int hashCode() {
        int result = dailyLogFolder.hashCode();
        result = 31 * result + updateFolder.hashCode();
        return result;
    }

    @Override
    public int compareTo(final LoggedUpdateId other) {
        return new CompareToBuilder()
                .append(dailyLogFolder, other.dailyLogFolder)
                .append(updateFolder, other.updateFolder)
                .toComparison();
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", dailyLogFolder, File.separator, updateFolder);
    }
}
