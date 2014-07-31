package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.api.rest.domain.WhoisVersionsInternal;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VersionObjectMapper {
    public static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    private final String baseUrl;

    @Autowired
    public VersionObjectMapper(@Value("${api.rest.baseurl}") final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public WhoisResources mapVersions(final String objectType, final String pkey, final String source, final List<ObjectVersion> versions) {
        final WhoisVersionsInternal whoisVersions = new WhoisVersionsInternal(
                objectType,
                pkey,
                mapVersionsInternal(versions, source));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    public List<WhoisVersionInternal> mapVersionsInternal(final List<ObjectVersion> versions, final String source) {
        final List<WhoisVersionInternal> whoisVersions = Lists.newArrayList();

        for (ObjectVersion version : versions) {
            whoisVersions.add(mapVersion(version, source));
        }
        return whoisVersions;
    }

    public WhoisVersionInternal mapVersion(final ObjectVersion objectVersion, final String source) {
        final Interval interval = objectVersion.getInterval();

        return new WhoisVersionInternal(
                objectVersion.getRevision(),
                objectVersion.getType().getName().toUpperCase(),
                objectVersion.getPkey().toString(),
                ISO8601_FORMATTER.print(interval.getStart()),
                interval.getEnd().getMillis() != Long.MAX_VALUE ? ISO8601_FORMATTER.print(interval.getEnd()) : null,
                createWhoisVersionInternalLink(source, objectVersion.getType().getName().toUpperCase(), objectVersion.getPkey() + "/" + objectVersion.getRevision()));
    }

    private Link createWhoisVersionInternalLink(String source, String type, String versionId) {
        return new Link("locator", String.format("%s/api/rnd/%s/%s/%s", baseUrl, source, type, versionId));
    }

    public List<WhoisVersionInternal> mapObjectReferences(final List<ObjectVersion> versions, String source) {
        if (CollectionUtils.isEmpty(versions)) {
            return null;
        }

        final List<WhoisVersionInternal> whoisVersions = Lists.newArrayList();
        for (ObjectVersion version : versions) {
            whoisVersions.add(mapVersionWithTypeAndKey(version, source));
        }
        return whoisVersions;
    }

    public WhoisVersionInternal mapVersionWithTypeAndKey(final ObjectVersion objectVersion, final String source) {
        final WhoisVersionInternal version = mapVersion(objectVersion, source);
        version.setType(objectVersion.getType().getName());
        version.setKey(objectVersion.getPkey().toString());
        return version;
    }
}
