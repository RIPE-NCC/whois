package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.rest.WhoisVersionInternal;
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
    private final AttributeMapper attributeMapper;

    @Autowired
    public VersionObjectMapper(@Value("${api.rest.rnd.baseurl}") final String baseUrl) {
        this.baseUrl = baseUrl;
        attributeMapper = new FormattedClientAttributeMapper();
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

    public WhoisObject mapObject(final RpslObject rpslObject, final String source) {
        final WhoisObject whoisObject = new WhoisObject();
        final List<Attribute> as = Lists.newArrayList();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            as.addAll(attributeMapper.map(attribute, source));
        }
        whoisObject.setAttributes(as);
        return whoisObject;
    }
}
