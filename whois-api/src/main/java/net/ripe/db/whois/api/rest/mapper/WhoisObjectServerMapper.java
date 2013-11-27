package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhoisObjectServerMapper extends AbstractWhoisObjectMapper {
    private final ReferencedTypeResolver referencedTypeResolver;

    @Autowired
    public WhoisObjectServerMapper(final ReferencedTypeResolver referencedTypeResolver, @Value("${api.rest.baseurl}") final String baseUrl) {
        super(baseUrl);
        this.referencedTypeResolver = referencedTypeResolver;
    }

    @Override
    Attribute buildAttribute(RpslAttribute attribute, final CIString value, final String comment, final String source) {
        // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
        final String referencedType = (attribute.getType() != null && referencedTypeResolver != null) ? referencedTypeResolver.getReferencedType(attribute.getType(), value) : null;
        final Link link = (referencedType != null) ? createLink(source, referencedType, value.toString()) : null;
        return createAttribute(attribute.getKey(), value.toString(), comment, referencedType, link);
    }

    public List<WhoisVersion> mapVersions(final List<DeletedVersionResponseObject> deleted, final List<VersionResponseObject> versions) {
        final List<WhoisVersion> whoisVersions = Lists.newArrayList();
        for (final DeletedVersionResponseObject deletedVersion : deleted) {
            whoisVersions.add(new WhoisVersion(deletedVersion.getDeletedDate().toString()));
        }

        for (final VersionResponseObject version : versions) {
            whoisVersions.add(new WhoisVersion(version.getOperation() == Operation.UPDATE ? "ADD/UPD" : "DEL", version.getDateTime().toString(), version.getVersion()));
        }

        return whoisVersions;
    }

    public WhoisObject map(final RpslObject rpslObject, final List<TagResponseObject> tags) {
        final WhoisObject object = map(rpslObject);

        final List<WhoisTag> whoisTags = Lists.newArrayListWithExpectedSize(tags.size());
        for (final TagResponseObject tag : tags) {
            whoisTags.add(new WhoisTag(tag.getType().toString(), tag.getValue()));
        }
        object.setTags(whoisTags);
        return object;
    }
}
