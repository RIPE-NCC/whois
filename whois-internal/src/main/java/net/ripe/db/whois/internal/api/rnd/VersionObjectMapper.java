package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.internal.api.rnd.rest.WhoisVersionInternal;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VersionObjectMapper {
    public static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    private final String baseUrl;

    @Autowired
    public VersionObjectMapper(@Value("${api.rest.rnd.baseurl}") final String baseUrl) {
        this.baseUrl = baseUrl;
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
}
