package net.ripe.db.whois.common.grs;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import org.slf4j.Logger;

import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

class AuthoritativeResourceLoader {
    private static final Pattern RESOURCELINE = Pattern.compile("^([a-zA-Z]+)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)(?:\\|.*|$)");
    private static final Set<String> ALLOWED_STATUS = Sets.newHashSet("allocated", "assigned", "available", "reserved");

    private final Logger logger;
    private final String name;
    private final Scanner scanner;

    private final Set<CIString> autNums = Sets.newHashSet();
    private final IntervalMap<Ipv4Resource, Ipv4Resource> inetnums = new NestedIntervalMap<>();
    private final IntervalMap<Ipv6Resource, Ipv6Resource> inet6nums = new NestedIntervalMap<>();

    public AuthoritativeResourceLoader(final Logger logger, final String name, final Scanner scanner) {
        this.logger = logger;
        this.name = name;
        this.scanner = scanner;
    }

    public AuthoritativeResource load() {
        scanner.useDelimiter("\n");

        final String expectedSource = name.replace("-GRS", "").toLowerCase();

        while (scanner.hasNext()) {
            final String line = scanner.next();
            handleLine(expectedSource, line);
        }

        return new AuthoritativeResource(logger, autNums, inetnums, inet6nums);
    }

    private void handleLine(final String expectedSource, final String line) {
        final Matcher matcher = RESOURCELINE.matcher(line);
        if (!matcher.matches()) {
            logger.debug("Skipping: {}", line);
            return;
        }

        final String source = matcher.group(1);
        final String cc = matcher.group(2);
        final String type = matcher.group(3).toLowerCase();
        final String start = matcher.group(4);
        final String value = matcher.group(5);
        final String status = matcher.group(7).toLowerCase();

        if (!source.toLowerCase().contains(expectedSource)) {
            logger.debug("Ignoring source '{}': {}", source, line);
            return;
        }

        if (cc.indexOf('*') != -1) {
            logger.debug("Ignoring country code '{}': {}", cc, line);
            return;
        }

        if (!ALLOWED_STATUS.contains(status)) {
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
        final int startNum = Integer.parseInt(start);
        final int count = Integer.parseInt(value);
        for (int i = 0; i < count; i++) {
            autNums.add(ciString(String.format("AS%s", startNum + i)));
        }
    }

    private void createIpv4Resource(final String start, final String value) {
        final long begin = Ipv4Resource.parse(start).begin();
        final long end = begin + (Long.parseLong(value) - 1);
        final Ipv4Resource ipv4Resource = new Ipv4Resource(begin, end);
        inetnums.put(ipv4Resource, ipv4Resource);
    }

    private void createIpv6Resource(final String start, final String value) {
        final Ipv6Resource ipv6Resource = Ipv6Resource.parse(String.format("%s/%s", start, value));
        inet6nums.put(ipv6Resource, ipv6Resource);
    }
}

