package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.api.rest.domain.WhoisVersionsInternal;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VersionObjectMapper {
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
        final DateTime start = interval.getStart();
        final DateTime end = interval.getEnd();
        return new WhoisVersionInternal(
                objectVersion.getRevision(),
                start.toString(),
                end.toString(),
                createWhoisVersionInternalLink(source, objectVersion.getType().getName(), objectVersion.getPkey() + "/" + objectVersion.getRevision()));
    }

    private Link createWhoisVersionInternalLink(String source, String type, String versionId) {
        return new Link("locator", String.format("%s/api/rnd/%s/%s/%s", baseUrl, source, type, versionId));
    }
}
