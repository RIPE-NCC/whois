package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import org.slf4j.Logger;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class AuthoritativeResourceLoader extends AbstractAuthoritativeResourceLoader {

    private static final Splitter PIPE_SPLITTER = Splitter.on('|');

    private final String name;
    private final Scanner scanner;

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner) {
        super(logger);
        this.name = name;
        this.scanner = scanner;
    }

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner, final Set<AuthoritativeResourceStatus> statuses) {
        super(logger, statuses);
        this.name = name;
        this.scanner = scanner;
    }

    public AuthoritativeResource load() {
        scanner.useDelimiter("\n");

        final String expectedSource = name.toLowerCase().replace("-grs", "");

        while (scanner.hasNext()) {
            final String line = scanner.next();
            handleLine(expectedSource, line);
        }

        return new AuthoritativeResource(autNums, ipv4Space, ipv6Space);
    }

    private void handleLine(final String expectedSource, final String line) {

        final List<String> columns = PIPE_SPLITTER.splitToList(line);

        if (columns.size() < 7) {
            logger.debug("Skipping, not enough columns: {}", line);
            return;
        }

        final String source = columns.get(0);
        final String cc = columns.get(1);
        final String type = columns.get(2).toLowerCase();
        final String start = columns.get(3);
        final String value = columns.get(4);

        AuthoritativeResourceStatus status;
        try {
            status = AuthoritativeResourceStatus.valueOf(columns.get(6).toUpperCase());
        } catch (IllegalArgumentException iae) {
            logger.debug("Ignoring status '{}'", columns.get(6));
            return;
        }

        handleResource(source,
                       cc,
                       type,
                       start,
                       value,
                       status,
                       expectedSource);
    }

}

