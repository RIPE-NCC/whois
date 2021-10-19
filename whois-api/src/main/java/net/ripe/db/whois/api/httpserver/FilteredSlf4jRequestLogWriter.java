package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.server.Slf4jRequestLogWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public class FilteredSlf4jRequestLogWriter extends Slf4jRequestLogWriter {
    private final String keyToFilter;
    // Replace value passed to apikey with "FILTERED" but leave the last 3 characters if its API keys
    private static final Pattern ApiKeyPattern = Pattern.compile("(?<=(?i)(apikey=))(.+?(?=\\\\S{3}\\\\s))");
    private static final Pattern PasswordPattern = Pattern.compile("(?<=(?i)(password=))([^&]*)");

    public FilteredSlf4jRequestLogWriter(String keyToFilter) {
        this.keyToFilter = keyToFilter;
    }

    @Override
    public void write(String requestEntry) throws IOException {
        String filtered;

        if (keyToFilter != null && keyToFilter.equalsIgnoreCase("apikey")) {
            filtered = ApiKeyPattern.matcher(requestEntry).replaceAll("FILTERED");
        } else {
            filtered = PasswordPattern.matcher(requestEntry).replaceAll("FILTERED");
        }

        super.write(filtered);
    }

}
