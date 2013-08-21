package net.ripe.db.whois.api.whois;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.domain.Attribute;
import net.ripe.db.whois.api.whois.domain.Link;
import net.ripe.db.whois.api.whois.domain.Source;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.api.whois.domain.WhoisTag;
import net.ripe.db.whois.api.whois.domain.WhoisVersion;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WhoisObjectMapper {

    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private static final Pattern COMMENT_PATTERN = Pattern.compile("(?m)^[^#]*[#](.*)$");

    private static final Set<AttributeType> CSV_ATTRIBUTES = Sets.immutableEnumSet(
            AttributeType.MNT_BY,
            AttributeType.MNT_LOWER,
            AttributeType.MNT_DOMAINS,
            AttributeType.REFERRAL_BY,
            AttributeType.MNT_REF,
            AttributeType.MEMBERS,
            AttributeType.MP_MEMBERS,
            AttributeType.MBRS_BY_REF,
            AttributeType.MEMBER_OF);

    private static final Splitter CSV_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private final ReferencedTypeResolver referencedTypeResolver;
    private final String baseUrl;

    @Autowired
    public WhoisObjectMapper(final ReferencedTypeResolver referencedTypeResolver, @Value("${api.rest.lookup.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.baseUrl = baseUrl;
    }

    // TODO: [AH] converting between object by parse(toString()) is the most inefficient; reimplement using direct translation
    public RpslObject map(final WhoisObject whoisObject) {
        final StringBuilder builder = new StringBuilder();
        for (Attribute attribute : whoisObject.getAttributes()) {

            Iterable<String> values;

            AttributeType attributeType = AttributeType.getByName(attribute.getName());
            if (CSV_ATTRIBUTES.contains(attributeType)) {
                values = CSV_SPLITTER.split(attribute.getValue());  // TODO: [AH] don't do this! see getCleanValues()
            } else {
                values = Collections.singletonList(attribute.getValue());
            }

            for (String value : values) {
                builder.append(attribute.getName());
                builder.append(": ");
                builder.append(value);
                if (attribute.getComment() != null) {
                    builder.append(" # ");
                    builder.append(attribute.getComment());
                }
                builder.append('\n');
            }
        }

        return RpslObject.parse(builder.toString());
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

        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            final String comment = getComment(attribute);
            for (CIString value : attribute.getCleanValues()) {
                if (value.length() > 0) {
                    final String referencedType = (attribute.getType() != null) ? referencedTypeResolver.getReferencedType(attribute.getType(), value) : null;
                    final Link link = (referencedType != null) ? createLink(source, referencedType, value.toString()) : null;
                    attributes.add(createAttribute(attribute.getKey(), value.toString(), comment, referencedType, link));
                }
            }
        }

        final RpslAttribute primaryKeyRpslAttribute = getPrimaryKey(rpslObject);
        final String primaryKeyName = primaryKeyRpslAttribute.getKey();
        final String primaryKeyValue = primaryKeyRpslAttribute.getCleanValue().toString();
        final Attribute primaryKeyAttribute = createAttribute(primaryKeyName, primaryKeyValue, null, null, null);
        final List<Attribute> primaryKeyAttributes = Lists.newArrayList(primaryKeyAttribute);

        return createWhoisObject(
                createSource(source),
                type,
                attributes,
                primaryKeyAttributes,
                createLink(rpslObject));
    }

    @Nullable
    private String getComment(final RpslAttribute attribute) {
        Matcher m = COMMENT_PATTERN.matcher(attribute.getValue());
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private Link createLink(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final String key = rpslObject.getKey().toString();
        return createLink(source, type, key);
    }

    private Link createLink(final String source, final String type, final String key) {
        return new Link("locator", String.format("%s/%s/%s/%s", baseUrl, source, type, key));
    }

    private Source createSource(final String id) {
        return new Source(id);
    }

    private RpslObject filter(final RpslObject rpslObject) {
        return FILTER_AUTH_FUNCTION.apply(rpslObject);
    }

    private RpslAttribute getPrimaryKey(final RpslObject rpslObject) {
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

    private Attribute createAttribute(final String name, final String value, final String comment, final String referencedType, final Link link) {
        final Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setValue(value);
        attribute.setComment(comment);
        attribute.setReferencedType(referencedType);
        attribute.setLink(link);
        return attribute;
    }

    private WhoisObject createWhoisObject(final Source source, final String type, final List<Attribute> attributes, final List<Attribute> primaryKey, final Link link) {
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
