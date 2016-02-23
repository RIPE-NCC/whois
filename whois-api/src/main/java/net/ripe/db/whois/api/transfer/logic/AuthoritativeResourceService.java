package net.ripe.db.whois.api.transfer.logic;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Transfers In:
 * - only update data related to the current (master) region. Don't update other regions data.
 */
@Component
public class AuthoritativeResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceService.class);

    private static final Pattern IPV4_PATTERN = Pattern.compile("\\d+[.]\\d+[.]\\d+[.]\\d+[-]\\d+[.]\\d+[.]\\d+[.]\\d+");

    private final AuthoritativeResourceDao authoritativeResourceDao;
    private final ResourceDataDao resourceDataDao;
    private final String source;

    @Autowired
    public AuthoritativeResourceService(
            final AuthoritativeResourceDao authoritativeResourceDao,
            final ResourceDataDao resourceDataDao,
            @Value("${whois.source}") final String source) {
        this.authoritativeResourceDao = authoritativeResourceDao;
        this.resourceDataDao = resourceDataDao;
        this.source = source;
    }

    @Transactional
    public void transferInIpv4Block(final String range) {
        final AuthoritativeResource resources = resourceDataDao.load(source);

        final Iterable<Ipv4Range> overlaps = containsOrOverlaps(resources, range);
        if (isEmpty(overlaps)) {
            authoritativeResourceDao.create(source, formatIpv4Resource(range));
        } else {
            final Ipv4Range ipv4Range = Ipv4Range.parse(range);

            for (Ipv4Range overlap : overlaps) {
                if (overlap.contains(ipv4Range)) {
                    // found parent which completely overlaps range, no need to create a new one
                    break;
                } else {
                    for (Ipv4Range nonOverlap : ipv4Range.exclude(overlap)) {
                        // create new entry for non-overlapping range
                        authoritativeResourceDao.create(source, nonOverlap.toStringInRangeNotation());
                    }
                }
            }
        }
    }

    @Transactional
    public void transferOutIpv4Block(final String range) {
        final AuthoritativeResource resources = resourceDataDao.load(source);

        final Iterable<Ipv4Range> overlaps = containsOrOverlaps(resources, range);
        if (isEmpty(overlaps)) {
            authoritativeResourceDao.delete(source, formatIpv4Resource(range));
        } else {
            final Ipv4Range ipv4Range = Ipv4Range.parse(range);

            for (Ipv4Range overlap : overlaps) {
                if (overlap.contains(ipv4Range)) {
                    // found a parent range
                    authoritativeResourceDao.delete(source, overlap.toStringInRangeNotation());

                    for (Ipv4Range nonOverlap : overlap.exclude(ipv4Range)) {
                        // create new entry for non-overlapping range
                        authoritativeResourceDao.create(source, nonOverlap.toStringInRangeNotation());
                    }
                } else {
                    if (overlap.overlaps(ipv4Range)) {
                        authoritativeResourceDao.delete(source, overlap.toStringInRangeNotation());

                        for (Ipv4Range nonOverlap : overlap.exclude(ipv4Range)) {
                            // create new entry for non-overlapping range
                            authoritativeResourceDao.create(source, nonOverlap.toStringInRangeNotation());
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public void transferInAsBlock(final String asBlock) {
        authoritativeResourceDao.create(source.toLowerCase(), formatAsBlock(asBlock));
    }

    @Transactional
    public void transferOutAsBlock(final String asBlock) {
        authoritativeResourceDao.delete(source.toLowerCase(), formatAsBlock(asBlock));
    }

    private Iterable<Ipv4Range> containsOrOverlaps(final AuthoritativeResource resources, final String range) {
        final Ipv4Range ipv4Range = Ipv4Range.parse(range);
        return Iterables.filter(extractIpv4Resources(resources), new Predicate<Ipv4Range>() {
            @Override
            public boolean apply(final Ipv4Range input) {
                return input.contains(ipv4Range) || input.overlaps(ipv4Range);
            }
        });
    }

    private SortedRangeSet<Ipv4, Ipv4Range> extractIpv4Resources(final AuthoritativeResource resources) {
        final SortedRangeSet<Ipv4, Ipv4Range> results = new SortedRangeSet<>();
        for (String resource : resources.getResources()) {
            if (IPV4_PATTERN.matcher(resource).matches()) {
                results.add(Ipv4Range.parse(resource));
            }
        }

        return results;
    }


    private String formatIpv4Resource(final String range) {
        return Ipv4Range.parse(range).toStringInRangeNotation();
    }

    private String formatAsBlock(final String asBlock) {
        return AsnRange.parse(asBlock).toString();
    }

    private boolean isEmpty(final Iterable iterable) {
        return !iterable.iterator().hasNext();
    }
}
