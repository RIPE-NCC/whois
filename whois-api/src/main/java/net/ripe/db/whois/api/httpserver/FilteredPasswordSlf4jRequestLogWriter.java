package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.server.Slf4jRequestLogWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.regex.Pattern;


public class FilteredPasswordSlf4jRequestLogWriter extends Slf4jRequestLogWriter {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?<=(?i)(password=))([^&]*)");

    @Override
    public void write(@Nonnull final String requestEntry) throws IOException {
        //super.write(PasswordFilter.filterPasswordsInUrl(requestEntry));
        super.write(PASSWORD_PATTERN.matcher(requestEntry).replaceAll("FILTERED"));
    }

}
