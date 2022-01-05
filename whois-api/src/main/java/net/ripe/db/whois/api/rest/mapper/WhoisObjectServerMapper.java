package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.search.AbuseContactSearch;
import net.ripe.db.whois.api.rest.search.ManagedAttributeSearch;
import net.ripe.db.whois.api.rest.search.ResourceHolderSearch;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.getServerAttributeMapper;

@Component
public class WhoisObjectServerMapper {
    private final WhoisObjectMapper whoisObjectMapper;
    private final ResourceHolderSearch resourceHolderSearch;
    private final AbuseContactSearch abuseContactSearch;
    private final ManagedAttributeSearch managedAttributeSearch;

    @Autowired
    public WhoisObjectServerMapper(
            final WhoisObjectMapper whoisObjectMapper,
            final ResourceHolderSearch resourceHolderSearch,
            final AbuseContactSearch abuseContactSearch,
            final ManagedAttributeSearch managedAttributeSearch) {
        this.whoisObjectMapper = whoisObjectMapper;
        this.resourceHolderSearch = resourceHolderSearch;
        this.abuseContactSearch = abuseContactSearch;
        this.managedAttributeSearch = managedAttributeSearch;
    }

    public List<WhoisVersion> mapVersions(final List<DeletedVersionResponseObject> deleted, final List<VersionResponseObject> versions) {
        final List<WhoisVersion> whoisVersions = Lists.newArrayList();
        for (final DeletedVersionResponseObject deletedVersion : deleted) {
            whoisVersions.add(new WhoisVersion(deletedVersion.getDeletedDate().toString()));
        }

        for (final VersionResponseObject version : versions) {
            whoisVersions.add(new WhoisVersion(version.getOperation() == Operation.UPDATE ? "ADD/UPD" : "DEL", version.getDateTime().toString(),
                    version.getVersion()));
        }

        return whoisVersions;
    }

    public WhoisObject map(final RpslObject rpslObject, final Parameters parameters) {
        final Class<? extends AttributeMapper> attributeMapper = getServerAttributeMapper(Boolean.TRUE.equals(parameters.getUnformatted()));
        return whoisObjectMapper.map(rpslObject, attributeMapper);
    }

    public void mapTags(final WhoisObject whoisObject, final TagResponseObject tagResponseObject) {
        if (tagResponseObject != null && !tagResponseObject.getTags().isEmpty()) {
            final List<Tag> tags = tagResponseObject.getTags();
            final List<WhoisTag> whoisTags = Lists.newArrayListWithExpectedSize(tags.size());
            for (final Tag tag : tags) {
                whoisTags.add(new WhoisTag(tag.getType().toString(), tag.getValue()));
            }
            whoisObject.setTags(whoisTags);
        }
    }

    public void mapResourceHolder(final WhoisObject whoisObject, final Parameters parameters, final RpslObject rpslObject) {
        if (Boolean.TRUE.equals(parameters.getResourceHolder())) {
            whoisObject.setResourceHolder(resourceHolderSearch.findResourceHolder(rpslObject));
        }
    }

    public void mapAbuseContact(final WhoisObject whoisObject, final Parameters parameters, final RpslObject rpslObject) {
        if (Boolean.TRUE.equals(parameters.getAbuseContact())) {
            whoisObject.setAbuseContact(abuseContactSearch.findAbuseContact(rpslObject));
        }
    }

    public void mapManagedAttributes(final WhoisObject whoisObject, final Parameters parameters, final RpslObject rpslObject) {
        if (Boolean.TRUE.equals(parameters.getManagedAttributes())) {
            whoisObject.setManaged(managedAttributeSearch.isCoMaintained(rpslObject));
            if (Boolean.TRUE.equals(whoisObject.isManaged())) {
                final Iterator<Attribute> attributeIterator = whoisObject.getAttributes().iterator();
                final Iterator<RpslAttribute> rpslAttributeIterator = rpslObject.getAttributes().iterator();
                while (attributeIterator.hasNext() && rpslAttributeIterator.hasNext()) {
                    final Attribute attribute = attributeIterator.next();
                    final RpslAttribute rpslAttribute = rpslAttributeIterator.next();
                    if (managedAttributeSearch.isRipeNccMaintained(rpslObject, rpslAttribute)) {
                        attribute.setManaged(Boolean.TRUE);
                    }
                }
            }
        }
    }

}
