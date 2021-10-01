package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.server.Slf4jRequestLogWriter;

import java.io.IOException;

public class FilteredSlf4jRequestLogWriter extends Slf4jRequestLogWriter {
    private final String keyToFilter;

    public FilteredSlf4jRequestLogWriter(String keyToFilter) {
        this.keyToFilter = keyToFilter;
    }

    @Override
    public void write(String requestEntry) throws IOException {
        String regexString;

        if (keyToFilter != null && keyToFilter.equalsIgnoreCase("apikey")) {
            // Replace with "FILTERED" but leave the last 3 characters if its API keys
            regexString = "(?<=(?i)(key=))(.+?(?=\\S{3}\\s))".replace("key", keyToFilter);
        } else {
            regexString = "(?<=(?i)(key=))(\\S*)".replace("key", keyToFilter);
        }

        String filtered = requestEntry.replaceAll(regexString, "FILTERED");
        super.write(filtered);
    }

}
