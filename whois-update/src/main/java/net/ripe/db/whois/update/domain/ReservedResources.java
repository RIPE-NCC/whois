package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import org.apache.commons.lang3.LongRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.rpsl.AttributeType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INETNUM;

@Component
public class ReservedResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservedResources.class);

    private final List<LongRange> reservedAsnumbers;
    private final Set<Interval> bogons;
    private final Set<IpInterval> administrativeRanges;

    private static final String TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE = "2002-06-25T14:19:09Z";

    @Autowired
    public ReservedResources(@Value("${whois.reserved.as.numbers:}") final String reservedAsNumbers, @Value("${ip.administrative:}") final String administrativeRanges, @Value("${ipranges.bogons:}") final String ... bogons) {
        this.reservedAsnumbers = parseReservedAsNumbers(reservedAsNumbers);
        this.administrativeRanges = parseAdministrativeBlocks(administrativeRanges);
        this.bogons = parseBogonPrefixes(bogons);
    }

    public boolean isReservedAsNumber(final Long asn) {
        for (LongRange range : this.reservedAsnumbers) {
            if (range.contains(asn)) {
                return true;
            }
            if (asn < range.getMinimum()) {
                break;
            }
        }
        return false;
    }

    public boolean isReservedAsBlock(final String asBlock) {

        try {
            final AsBlockRange asBlockRange = AsBlockRange.parse(asBlock);
            final LongRange asLongRange = LongRange.of(asBlockRange.getBegin(), asBlockRange.getEnd());

            return this.reservedAsnumbers.stream()
                    .anyMatch(reservedAsn -> reservedAsn.isOverlappedBy(asLongRange));

        } catch (AttributeParseException ex) {
            throw new InternalServerErrorException("Invalid AS Block Range");
        }
    }

    private List<LongRange> parseReservedAsNumbers(final String reservedAsNumbers) {
        final List<LongRange> parsedAsNumbers = Lists.newArrayList();

        for (String reservedAsNumber : reservedAsNumbers.split(",")) {
            if (reservedAsNumber.contains("-")) {
                String[] startEnd = reservedAsNumber.split("-");
                parsedAsNumbers.add(LongRange.of(Long.parseLong(startEnd[0]), Long.parseLong(startEnd[1])));
            } else {
                parsedAsNumbers.add(LongRange.of(Long.parseLong(reservedAsNumber), Long.parseLong(reservedAsNumber)));
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

    private Set<IpInterval> parseAdministrativeBlocks(final String administrativeRanges) {
        try {
            return Arrays.stream(administrativeRanges.split(",")).map(IpInterval::parse).collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("{} is not a valid prefix, skipping...", administrativeRanges);
            return Sets.newHashSet();
        }
    }

    @Nullable
    public RpslObject getAdministrativeRange(final String prefix) {

        final IpInterval<?> interval;
        try {
            interval = IpInterval.parse(prefix);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("{} is not a valid prefix, skipping...", prefix);
            return null;
        }

        if(isBogon(prefix)) return null;

        final IpInterval<?> administrativeBlock =  administrativeRanges.stream()
                .filter(range -> interval.getClass().equals(range.getClass()) && range.contains(interval))
                .findAny().orElse(null);

        return administrativeBlock != null ?
                new RpslObjectBuilder().append(new RpslAttribute( (administrativeBlock instanceof  Ipv4Resource) ? INETNUM : INET6NUM, administrativeBlock.toString()))
                        .append(new RpslAttribute(AttributeType.NETNAME, "RIPE-NCC-MANAGED-ADDRESS-BLOCK"))
                        .append(new RpslAttribute(AttributeType.STATUS, InetnumStatus.ALLOCATED_UNSPECIFIED.toString()))
                        .append(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.SOURCE, "RIPE"))
                        .get() : null;
    }

    public boolean isAdministrative(final String prefix) {
        try {
            return administrativeRanges.contains(IpInterval.parse(prefix));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
