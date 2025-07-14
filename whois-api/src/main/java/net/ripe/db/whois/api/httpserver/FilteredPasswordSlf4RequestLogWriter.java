package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.common.conversion.PasswordFilter;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class FilteredPasswordSlf4RequestLogWriter extends Slf4jRequestLogWriter {

    @Override
    public void write(@Nonnull final String requestEntry) throws IOException {
        super.write(PasswordFilter.filterPasswordsInUrl(requestEntry));
    }

}
