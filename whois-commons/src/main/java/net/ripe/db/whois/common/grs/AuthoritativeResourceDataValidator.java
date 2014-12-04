package net.ripe.db.whois.common.grs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
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
            @Value("${grs.sources}") final String[] grsSourceNames,
            final AuthoritativeResourceData authoritativeResourceData) {
        this.sources = Lists.newArrayList(ciSet(grsSourceNames));
        this.authoritativeResourceData = authoritativeResourceData;

        int maxSourceLength = 0;
        for (final CIString source : sources) {
            maxSourceLength = Math.max(maxSourceLength, source.length());
        }

        int maxObjectTypeLength = 0;
        for (final ObjectType objectType : ObjectType.values()) {
            maxObjectTypeLength = Math.max(maxObjectTypeLength, objectType.getName().length());
        }

        reportFormat = String.format("%%-%ds\t%%-%ds\t%%-%ds\t%%s\n", maxSourceLength, maxSourceLength, maxObjectTypeLength);
    }

    void checkOverlaps(final Writer writer) throws IOException {
        for (int i1 = 0; i1 < sources.size(); i1++) {
            for (int i2 = i1 + 1; i2 < sources.size(); i2++) {
                final CIString source1 = sources.get(i1);
                final CIString source2 = sources.get(i2);

                checkOverlaps(writer, source1, authoritativeResourceData.getAuthoritativeResource(source1), source2, authoritativeResourceData.getAuthoritativeResource(source2));
            }
        }
    }

    private void checkOverlaps(final Writer writer, final CIString source1, final AuthoritativeResource resource1, final CIString source2, final AuthoritativeResource resource2) throws IOException {
        checkOverlapsForIntervals(writer, source1, source2, resource1.findAutnumOverlaps(resource2), ObjectType.AUT_NUM);
        checkOverlapsForIntervals(writer, source1, source2, resource1.findInetnumOverlaps(resource2), ObjectType.INETNUM);
        checkOverlapsForIntervals(writer, source1, source2, resource1.findInet6numOverlaps(resource2), ObjectType.INET6NUM);
    }

    private void checkOverlapsForIntervals(final Writer writer, final CIString source1, final CIString source2,
                                           final Iterable<String> overlaps, final ObjectType objectType) throws IOException {
        for (String overlap : overlaps) {
            reportOverlap(writer, source1, source2, objectType, overlap);
        }
    }

    private void reportOverlap(final Writer writer, final CIString source1, final CIString source2, final ObjectType type, final String resource) throws IOException {
        writer.write(String.format(reportFormat, source1, source2, type.getName(), resource));
    }
}
