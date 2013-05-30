package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciSet;

@Component
class AuthoritativeResourceDataValidator {
    private final static int MAX_RESOURCE_LENGTH = 40;
    private final List<CIString> sources;
    private final AuthoritativeResourceData authoritativeResourceData;
    private final String reportFormat;

    @Autowired
    AuthoritativeResourceDataValidator(
            @Value("${grs.sources}") final String grsSourceNames,
            final AuthoritativeResourceData authoritativeResourceData) {
        this.sources = Lists.newArrayList(ciSet(Splitter.on(',').split(grsSourceNames)));
        this.authoritativeResourceData = authoritativeResourceData;

        int maxSourceLength = 0;
        for (final CIString source : sources) {
            maxSourceLength = Math.max(maxSourceLength, source.length());
        }

        int maxObjectTypeLength = 0;
        for (final ObjectType objectType : ObjectType.values()) {
            maxObjectTypeLength = Math.max(maxObjectTypeLength, objectType.getName().length());
        }

        reportFormat = String.format("%%-%ds\t%%-%ds\t%%-%ds\t%%-%ds\t%%s\n", maxSourceLength, maxSourceLength, maxObjectTypeLength, MAX_RESOURCE_LENGTH);
    }

    void checkOverlaps(final Writer writer) throws IOException {
        final Map<CIString, AuthoritativeResource> authoritativeResourceMap = Maps.newHashMapWithExpectedSize(sources.size());
        for (final CIString source : sources) {
            authoritativeResourceMap.put(source, authoritativeResourceData.getAuthoritativeResource(source));
        }

        for (int i1 = 0; i1 < sources.size(); i1++) {
            for (int i2 = i1 + 1; i2 < sources.size(); i2++) {
                final CIString source1 = sources.get(i1);
                final CIString source2 = sources.get(i2);

                checkOverlaps(writer, source1, authoritativeResourceMap.get(source1), source2, authoritativeResourceMap.get(source2));
            }
        }
    }

    private void checkOverlaps(final Writer writer, final CIString source1, final AuthoritativeResource resource1, final CIString source2, final AuthoritativeResource resource2) throws IOException {
        checkOverlapsForAutNum(writer, source1, resource1, source2, resource2);
        checkOverlapsForIntervals(writer, source1, resource1.getInetRanges(), source2, resource2.getInetRanges(), ObjectType.INETNUM, Ipv4Resource.MAX_RANGE);
        checkOverlapsForIntervals(writer, source1, resource1.getInet6Ranges(), source2, resource2.getInet6Ranges(), ObjectType.INET6NUM, Ipv6Resource.MAX_RANGE);
    }

    private void checkOverlapsForAutNum(final Writer writer, final CIString source1, final AuthoritativeResource resource1, final CIString source2, final AuthoritativeResource resource2) throws IOException {
        final Set<CIString> overlappingAutNums = Sets.intersection(resource1.getAutNums(), resource2.getAutNums());
        for (final CIString overlappingAutNum : overlappingAutNums) {
            reportOverlap(writer, source1, source2, ObjectType.AUT_NUM, overlappingAutNum.toString(), overlappingAutNum.toString());
        }
    }

    private <E extends IpInterval<E>, M extends IntervalMap<E, E>> void checkOverlapsForIntervals(final Writer writer, final CIString source1, final M intervalMap1, final CIString source2, final M intervalMap2, final ObjectType objectType, final E parent) throws IOException {
        final List<E> intervals1 = intervalMap1.findExactAndAllMoreSpecific(parent);
        final List<E> intervals2 = intervalMap2.findExactAndAllMoreSpecific(parent);

        for (final E interval1 : intervals1) {
            for (final E interval2 : intervals2) {
                if (interval1.intersects(interval2)) {
                    reportOverlap(writer, source1, source2, objectType, interval1.toString(), interval2.toString());
                }
            }
        }
    }

    private void reportOverlap(final Writer writer, final CIString source1, final CIString source2, final ObjectType type, final String resource1, final String resource2) throws IOException {
        writer.write(String.format(reportFormat, source1, source2, type.getName(), resource1, resource2));
    }
}
