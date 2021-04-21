package net.ripe.db.whois.common.grs;

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

import java.util.Set;

abstract class AbstractAuthoritativeResourceLoader {

    private final static Set<String> ALLOWED_STATUSES = Sets.newHashSet("allocated", "assigned", "available", "reserved");

    protected final Logger logger;

    final SortedRangeSet<Asn, AsnRange> autNums = new SortedRangeSet<>();
    final SortedRangeSet<Ipv4, Ipv4Range> ipv4Space = new SortedRangeSet<>();
    final SortedRangeSet<Ipv6, Ipv6Range> ipv6Space = new SortedRangeSet<>();

    AbstractAuthoritativeResourceLoader(final Logger logger) {
        this.logger = logger;
    }

    void handleResource(final String source,
                        final String countryCode,
                        final String type,
                        final String start,
                        final String value,
                        final String status,
                        final String expectedSource) {

        if (!source.toLowerCase().contains(expectedSource)) {
            logger.debug("Ignoring source '{}', expected {}", source, expectedSource);
            return;
        }

        if (countryCode.indexOf('*') != -1) {
            logger.debug("Ignoring country code '{}'", countryCode);
            return;
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            logger.debug("Ignoring status '{}'", status);
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
                    logger.debug("Unsupported type '{}", type);
                    break;
            }
        } catch (RuntimeException ignored) {
            logger.warn("Unexpected '{}-{}'", ignored, ignored.getMessage());
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
