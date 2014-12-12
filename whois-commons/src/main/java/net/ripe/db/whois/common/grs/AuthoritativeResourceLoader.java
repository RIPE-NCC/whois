package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import org.slf4j.Logger;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class AuthoritativeResourceLoader {
    private final Logger logger;
    private final String name;
    private final Scanner scanner;
    private final Set<String> allowedStatus;

    private final SortedRangeSet<Asn, AsnRange> autNums = new SortedRangeSet<>();
    private final SortedRangeSet<Ipv4, Ipv4Range> ipv4Space = new SortedRangeSet<>();
    private final SortedRangeSet<Ipv6, Ipv6Range> ipv6Space = new SortedRangeSet<>();

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner) {
        this(logger, name, scanner, Sets.newHashSet("allocated", "assigned", "available", "reserved"));
    }

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner, final Set<String> allowedStatus) {
        this.logger = logger;
        this.name = name;
        this.scanner = scanner;
        this.allowedStatus = allowedStatus;
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

        if (!source.toLowerCase().contains(expectedSource)) {
            logger.debug("Ignoring source '{}': {}", source, line);
            return;
        }

        if (cc.indexOf('*') != -1) {
            logger.debug("Ignoring country code '{}': {}", cc, line);
            return;
        }

        if (!allowedStatus.contains(status)) {
            logger.debug("Ignoring status '{}': {}", status, line);
            return;
        }

        try {
            switch (type) {
                case "ipv4":
                    createIpv4Resource(start, value);
                    break;
                case "ipv6":
                    createIpv6Resource(start, value);
                    break;
                case "asn":
                    createAutNum(start, value);
                    break;
                default:
                    logger.debug("Unsupported type '{}': {}", type, line);
                    break;
            }
        } catch (RuntimeException ignored) {
            logger.warn("Unexpected '{}-{}': {}", ignored, ignored.getMessage(), line);
        }
    }

    private void createAutNum(final String start, final String value) {
        final long startNum = Long.parseLong(start);
        final long count = Long.parseLong(value);
        autNums.add(AsnRange.from(startNum).to(startNum + count - 1));
    }

    private void createIpv4Resource(final String start, final String value) {
        final long begin = Ipv4Resource.parse(start).begin();
        final long end = begin + (Long.parseLong(value) - 1);
        ipv4Space.add(Ipv4Range.from(begin).to(end));
    }

    private void createIpv6Resource(final String start, final String value) {
        ipv6Space.add(Ipv6Range.from(start).andPrefixLength(value));
    }
}

