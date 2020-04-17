package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import org.slf4j.Logger;

import java.util.List;
import java.util.Scanner;

public class AuthoritativeResourceLoader extends AbstractAuthoritativeResourceLoader {

    private final String name;
    private final Scanner scanner;

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner) {
        super(logger);
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

        final List<String> columns = Splitter.on('|').splitToList(line);

        if (columns.size() < 7) {
            logger.debug("Skipping, not enough columns: {}", line);
            return;
        }

        final String source = columns.get(0);
        final String cc = columns.get(1);
        final String type = columns.get(2).toLowerCase();
        final String start = columns.get(3);
        final String value = columns.get(4);
        final String status = columns.get(6).toLowerCase();

        handleResource(source,
                       cc,
                       type,
                       start,
                       value,
                       status,
                       expectedSource);
    }

}

