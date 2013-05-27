package net.ripe.db.whois.common.grs;

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

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class AuthoritativeResourceDataValidator {
    private final List<String> sources;
    private final AuthoritativeResourceData authoritativeResourceData;

    @Autowired
    AuthoritativeResourceDataValidator(
            @Value("${grs.sources}") final List<String> sources,
            final AuthoritativeResourceData authoritativeResourceData) {
        this.sources = Lists.newArrayList(sources);
        this.authoritativeResourceData = authoritativeResourceData;
    }

    void checkOverlaps(final Writer writer) throws IOException {
        final Map<String, AuthoritativeResource> authoritativeResourceMap = Maps.newHashMapWithExpectedSize(sources.size());
        for (final String source : sources) {
            authoritativeResourceMap.put(source, authoritativeResourceData.getAuthoritativeResource(ciString(source)));
        }

        for (int i1 = 0; i1 < sources.size(); i1++) {
            for (int i2 = i1 + 1; i2 < sources.size(); i2++) {
                final String source1 = sources.get(i1);
                final String source2 = sources.get(i2);

                checkOverlaps(writer, source1, authoritativeResourceMap.get(source1), source2, authoritativeResourceMap.get(source2));
            }
        }
    }

    private void checkOverlaps(final Writer writer, final String source1, final AuthoritativeResource resource1, final String source2, final AuthoritativeResource resource2) throws IOException {
        checkOverlapsForAutNum(writer, source1, resource1, source2, resource2);
        checkOverlapsForIntervals(writer, source1, resource1.getInetRanges(), source2, resource2.getInetRanges(), ObjectType.INETNUM, Ipv4Resource.MAX_RANGE);
        checkOverlapsForIntervals(writer, source1, resource1.getInet6Ranges(), source2, resource2.getInet6Ranges(), ObjectType.INET6NUM, Ipv6Resource.MAX_RANGE);
    }

    private void checkOverlapsForAutNum(final Writer writer, final String source1, final AuthoritativeResource resource1, final String source2, final AuthoritativeResource resource2) throws IOException {
        final Set<CIString> overlappingAutNums = Sets.intersection(resource1.getAutNums(), resource2.getAutNums());
        for (final CIString overlappingAutNum : overlappingAutNums) {
            reportOverlap(writer, source1, source2, ObjectType.AUT_NUM, overlappingAutNum.toString(), overlappingAutNum.toString());
        }
    }

    private <E extends IpInterval<E>, M extends IntervalMap<E, E>> void checkOverlapsForIntervals(final Writer writer, final String source1, final M intervalMap1, final String source2, final M intervalMap2, final ObjectType objectType, final E parent) throws IOException {
        final List<E> intervals = intervalMap1.findExactAndAllMoreSpecific(parent);
        for (final E interval : intervals) {
            reportOverlapsForInterval(writer, source1, source2, interval, intervalMap2.findExactOrFirstLessSpecific(interval), objectType);
            reportOverlapsForInterval(writer, source1, source2, interval, intervalMap2.findFirstMoreSpecific(interval), objectType);
        }
    }

    private <E extends IpInterval<E>> void reportOverlapsForInterval(final Writer writer, final String source1, final String source2, final E element, final List<E> elements, final ObjectType objectType) throws IOException {
        for (final E e : elements) {
            reportOverlap(writer, source1, source2, objectType, element.toString(), e.toString());
        }
    }

    private void reportOverlap(final Writer writer, final String source1, final String source2, final ObjectType type, final String resource1, final String resource2) throws IOException {
        writer.write(String.format("%-20s%-20s%-20s%-20s%s\n", source1, source2, type.getName(), resource1, resource2));
    }
}
