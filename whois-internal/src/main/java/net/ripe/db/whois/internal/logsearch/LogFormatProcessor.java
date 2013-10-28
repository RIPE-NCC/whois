package net.ripe.db.whois.internal.logsearch;

import java.io.IOException;

public interface LogFormatProcessor {
    public void addFileToIndex(String filePath) throws IOException;

    public void addDirectoryToIndex(String directoryPath) throws IOException;
}
