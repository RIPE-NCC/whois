package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.apache.commons.lang3.math.LongRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ReservedResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservedResources.class);

    private final List<LongRange> reservedAsnumbers;
    private final Set<Interval> bogons;

    @Autowired
    public ReservedResources(@Value("${whois.reserved.as.numbers:}") final String reservedAsNumbers, @Value("${ipranges.bogons:}") final String ... bogons) {
        this.reservedAsnumbers = parseReservedAsNumbers(reservedAsNumbers);
        this.bogons = parseBogonPrefixes(bogons);
    }

    public boolean isReservedAsNumber(final Long asn) {
        for (LongRange range : this.reservedAsnumbers) {
            if (range.containsLong(asn)) {
                return true;
            }
            if (asn < range.getMinimumLong()) {
                break;
            }
        }
        return false;
    }

    public boolean isReservedAsBlock(final String asBlock) {

        try {
            final AsBlockRange asBlockRange = AsBlockRange.parse(asBlock);
            final LongRange asLongRange = new LongRange(asBlockRange.getBegin(), asBlockRange.getEnd());

            return this.reservedAsnumbers.stream()
                    .anyMatch(reservedAsn -> reservedAsn.overlapsRange(asLongRange));

        } catch (AttributeParseException ex) {
            throw new InternalServerErrorException("Invalid AS Block Range");
        }
    }

    private List<LongRange> parseReservedAsNumbers(final String reservedAsNumbers) {
        final List<LongRange> parsedAsNumbers = Lists.newArrayList();

        for (String reservedAsNumber : reservedAsNumbers.split(",")) {
            if (reservedAsNumber.contains("-")) {
                String[] startEnd = reservedAsNumber.split("-");
                parsedAsNumbers.add(new LongRange(Long.parseLong(startEnd[0]), Long.parseLong(startEnd[1])));
            } else {
                parsedAsNumbers.add(new LongRange(Long.parseLong(reservedAsNumber), Long.parseLong(reservedAsNumber)));
            }
        }
        return parsedAsNumbers;
    }

    private Set<Interval> parseBogonPrefixes(final String ... bogons) {
        final Set<Interval> results = Sets.newHashSet();

        for (final String bogon : bogons) {
            try {
                results.add(IpInterval.parse(bogon));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("{} is not a valid prefix, skipping...", bogon);
            }
        }

        return results;
    }

    public boolean isBogon(final String prefix) {
        final Interval interval;
        try {
            interval = IpInterval.parse(prefix);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("{} is not a valid prefix, skipping...", prefix);
            return false;
        }

        for (final Interval bogon : bogons) {
            if (interval.getClass().equals(bogon.getClass()) && bogon.contains(interval)) {
                return true;
            }
        }

        return false;
    }
}
