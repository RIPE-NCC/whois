package net.ripe.db.whois.api.whois.mapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractWhoisObjectMapper {

    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private static final Pattern COMMENT_PATTERN = Pattern.compile("(?m)^[^#]*[#](.*)$");

    protected final String baseUrl;

    public AbstractWhoisObjectMapper(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RpslObject map(final WhoisObject whoisObject) {
        final List<RpslAttribute> rpslAttributes = Lists.newArrayList();
        for (final Attribute attribute : whoisObject.getAttributes()) {
            rpslAttributes.add(new RpslAttribute(attribute.getName(), attribute.getValue()));
        }

        return new RpslObject(rpslAttributes);
    }

    public WhoisResources map(final List<RpslObject> rpslObjects) {
        return map(rpslObjects, true);
    }

    public WhoisResources map(final List<RpslObject> rpslObjects, final boolean filter) {
        final WhoisResources whoisResources = new WhoisResources();
        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (RpslObject rpslObject : rpslObjects) {
            whoisObjects.add(map(rpslObject, filter));
        }
        whoisResources.setWhoisObjects(whoisObjects);
        return whoisResources;
    }

    public WhoisObject map(final RpslObject rpslObject) {
        return map(rpslObject, true);
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

    public WhoisObject map(final RpslObject rpslObject, final boolean filter) {
        if (filter) {
            return map(filter(rpslObject), false);
        }

        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final RpslAttribute primaryKeyRpslAttribute = getPrimaryKey(rpslObject);
        final String primaryKeyName = primaryKeyRpslAttribute.getKey();
        final String primaryKeyValue = primaryKeyRpslAttribute.getCleanValue().toString();
        final Attribute primaryKeyAttribute = createAttribute(primaryKeyName, primaryKeyValue, null, null, null);
        final List<Attribute> primaryKeyAttributes = Lists.newArrayList(primaryKeyAttribute);


        final List<Attribute> attributes = buildAttributes(rpslObject, source);

        return createWhoisObject(
                createSource(source),
                type,
                attributes,
                primaryKeyAttributes,
                createLink(rpslObject));
    }

    abstract List<Attribute> buildAttributes(RpslObject rpslObject, String source);

    @Nullable
    protected String getComment(final RpslAttribute attribute) {
        Matcher m = COMMENT_PATTERN.matcher(attribute.getValue());
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    protected Link createLink(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final String key = rpslObject.getKey().toString();
        return createLink(source, type, key);
    }

    protected Link createLink(final String source, final String type, final String key) {
        return new Link("locator", String.format("%s/%s/%s/%s", baseUrl, source, type, key));
    }

    protected Source createSource(final String id) {
        return new Source(id);
    }

    protected RpslObject filter(final RpslObject rpslObject) {
        return FILTER_AUTH_FUNCTION.apply(rpslObject);
    }

    protected RpslAttribute getPrimaryKey(final RpslObject rpslObject) {
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());
        Iterator<AttributeType> iterator = Sets.intersection(objectTemplate.getKeyAttributes(), objectTemplate.getLookupAttributes()).iterator();
        if (iterator.hasNext()) {
            AttributeType primaryKey = iterator.next();
            if (!iterator.hasNext()) {
                return rpslObject.findAttribute(primaryKey);
            }
        }

        throw new IllegalArgumentException("Couldn't find primary key attribute for type: " + rpslObject.getType());
    }

    protected Attribute createAttribute(final String name, final String value, final String comment, final String referencedType, final Link link) {
        final Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setValue(value);
        attribute.setComment(comment);
        attribute.setReferencedType(referencedType);
        attribute.setLink(link);
        return attribute;
    }

    protected WhoisObject createWhoisObject(final Source source, final String type, final List<Attribute> attributes, final List<Attribute> primaryKey, final Link link) {
        final WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(source);
        whoisObject.setType(type);
        whoisObject.setLink(link);
        whoisObject.setAttributes(attributes);
        whoisObject.setPrimaryKey(primaryKey);
        return whoisObject;
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
}
