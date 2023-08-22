package net.ripe.db.whois.common.grs;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.commons.ip.AbstractIpRange;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4RouteEntry;
import net.ripe.db.whois.common.iptree.Ipv6RouteEntry;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;

@Immutable
public class AuthoritativeResource {
    private static final Set<ObjectType> RESOURCE_TYPES = Sets.newEnumSet(Lists.newArrayList(ObjectType.AUT_NUM, ObjectType.INETNUM, INET6NUM), ObjectType.class);

    private final SortedRangeSet<Asn, AsnRange> autNums;
    private final SortedRangeSet<Ipv4, Ipv4Range> inetRanges;
    private final SortedRangeSet<Ipv6, Ipv6Range> inet6Ranges;

    public static AuthoritativeResource loadFromFile(final Logger logger, final String name, final Path path) {
        try (final Scanner scanner = new Scanner(path)) {
            return loadFromScanner(logger, name, scanner);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static AuthoritativeResource unknown() {
        return new AuthoritativeResource(new SortedRangeSet<>(), new SortedRangeSet<>(), new SortedRangeSet<>());
    }

    public static AuthoritativeResource loadFromScanner(final Logger logger, final String name, final Scanner scanner) {
        return new AuthoritativeResourceLoader(logger, name, scanner).load();
    }

    public static AuthoritativeResource loadFromScanner(final Logger logger, final String name, final Scanner scanner, final Set<AuthoritativeResourceStatus> statuses) {
        return new AuthoritativeResourceLoader(logger, name, scanner, statuses).load();
    }

    public AuthoritativeResource(final SortedRangeSet<Asn, AsnRange> autNums, final SortedRangeSet<Ipv4, Ipv4Range> inetRanges, final SortedRangeSet<Ipv6, Ipv6Range> inet6Ranges) {
        this.autNums = autNums;
        this.inetRanges = inetRanges;
        this.inet6Ranges = inet6Ranges;
    }

    public int getNrAutNums() {
        return autNums.size();
    }

    public int getNrInetnums() {
        return inetRanges.size();
    }

    public int getNrInet6nums() {
        return inet6Ranges.size();
    }

    boolean isEmpty() {
        return getNrAutNums() == 0 && getNrInetnums() == 0 && getNrInet6nums() == 0;
    }

    public boolean isMaintainedInRirSpace(final RpslObject rpslObject) {
        return isMaintainedInRirSpace(rpslObject.getType(), rpslObject.getKey());
    }

    public boolean isMaintainedInRirSpace(final ObjectType objectType, final CIString pkey) {
        try {
            switch (objectType) {
                case AUT_NUM:
                    return autNums.contains(parseAsn(pkey));
                case INETNUM:
                    return inetRanges.contains(parseRangeOrSingleAddress(pkey));
                case INET6NUM:
                    return inet6Ranges.contains(parseIpv6(pkey));
                default:
                    return true;
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Is this route(6) maintained in this RIR space?
     * We only consider the prefix, not the origin.
     * @param rpslObject the route(6)
     * @return true when route(6) is maintained in this RIR space
     */
    public boolean isRouteMaintainedInRirSpace(final RpslObject rpslObject) {
        return isRouteMaintainedInRirSpace(rpslObject.getType(), rpslObject.getKey());
    }

    public boolean isRouteMaintainedInRirSpace(final ObjectType objectType, CIString key) {
        try {
            switch (objectType) {
                case ROUTE:
                    return isRouteMaintainedInRirSpace(Ipv4RouteEntry.parse(key.toString(), 0));
                case ROUTE6:
                    return isRouteMaintainedInRirSpace(Ipv6RouteEntry.parse(key.toString(), 0));
            }
        } catch (IllegalArgumentException iae) {
            return true; // if route key parsing failed we can't determine if it's out of region
        }

        throw new IllegalArgumentException(String.format("%s is not a route", objectType));
    }

    private boolean isRouteMaintainedInRirSpace(final Ipv4RouteEntry routeEntry) {
        return isMaintainedInRirSpace(
                INETNUM,
                // [SB] TODO: yuck, refactor this at a later time, see AH's TODO in SearchKey
                ciString(routeEntry.getKey().toString())
        );
    }

    private boolean isRouteMaintainedInRirSpace(final Ipv6RouteEntry routeEntry) {
        return isMaintainedInRirSpace(
                INET6NUM,
                // [SB] TODO: yuck, refactor this at a later time, see AH's TODO in SearchKey
                ciString(routeEntry.getKey().toString())
        );
    }

    private AsnRange parseAsn(final CIString pkey) {
        return Asn.parse(pkey.toString()).asRange();
    }

    private Ipv6Range parseIpv6(final CIString pkey) {
        // use whois-common library to parse input
        // to keep backwards compatibility
        // so that 2001:2002:2003:2004:1::/65 is parsed as 2001:2002:2003:2004::/65
        final Ipv6Resource ipv6Resource = Ipv6Resource.parse(pkey);
        return Ipv6Range.from(ipv6Resource.begin()).to(ipv6Resource.end());
    }

    private Ipv4Range parseRangeOrSingleAddress(final CIString pkey) {
        // use whois-common library to parse input
        // to keep backwards compatibility
        // so that 10/8 is parsed as 10.0.0.0/8
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(pkey);
        return Ipv4Range.from(ipv4Resource.begin()).to(ipv4Resource.end());
    }

    public Set<ObjectType> getResourceTypes() {
        return RESOURCE_TYPES;
    }

    public Iterable<String> findAutnumOverlaps(AuthoritativeResource other) {
        return Iterables.transform(this.autNums.intersection(other.autNums), input -> input.toString());
    }

    public Iterable<String> findInetnumOverlaps(AuthoritativeResource other) {
        return Iterables.transform(this.inetRanges.intersection(other.inetRanges), input -> input.toStringInRangeNotation());
    }

    public Iterable<String> findInet6numOverlaps(AuthoritativeResource other) {
        return Iterables.transform(this.inet6Ranges.intersection(other.inet6Ranges), input -> input.toStringInCidrNotation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AuthoritativeResource that = (AuthoritativeResource) o;

        return Objects.equals(autNums, that.autNums) &&
                Objects.equals(inet6Ranges, that.inet6Ranges) &&
                Objects.equals(inetRanges, that.inetRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autNums, inetRanges, inet6Ranges);
    }

    public List<String> getResources() {
        return Lists.newArrayList(
            Iterables.concat(
                Iterables.transform(autNums, AsnRange::toString),
                Iterables.transform(inetRanges, AbstractIpRange::toStringInRangeNotation),
                Iterables.transform(Iterables.concat(Iterables.transform(inet6Ranges, AbstractIpRange::splitToPrefixes)), AbstractIpRange::toStringInCidrNotation)
            ));
    }

    public static AuthoritativeResource merge(final List<AuthoritativeResource> authoritativeResources) {
        final SortedRangeSet<Asn, AsnRange> asns = new SortedRangeSet<>();
        final SortedRangeSet<Ipv4, Ipv4Range> ipv4s = new SortedRangeSet<>();
        final SortedRangeSet<Ipv6, Ipv6Range> ipv6s = new SortedRangeSet<>();

        authoritativeResources.forEach(authoritativeResource -> {
            asns.addAll(authoritativeResource.autNums);
            ipv4s.addAll(authoritativeResource.inetRanges);
            ipv6s.addAll(authoritativeResource.inet6Ranges);
        });

        return new AuthoritativeResource(asns, ipv4s, ipv6s);
    }

}

