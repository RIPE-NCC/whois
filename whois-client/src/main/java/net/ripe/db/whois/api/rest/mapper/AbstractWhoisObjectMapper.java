package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractWhoisObjectMapper {

   protected final String baseUrl;

    public AbstractWhoisObjectMapper(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RpslObject map(final WhoisObject whoisObject) {
        final List<RpslAttribute> rpslAttributes = Lists.newArrayList();

        for (final Attribute attribute : whoisObject.getAttributes()) {
            String rpslValue;

            final String value = attribute.getValue();
            final String comment = attribute.getComment();
            if (StringUtils.isBlank(comment)) {
                rpslValue = value;
            } else {
                if (value.indexOf('#') >= 0) {
                    throw new IllegalArgumentException("Value cannot have a comment in " + attribute);
                }
                rpslValue = value + " # " + comment;
            }
            rpslAttributes.add(new RpslAttribute(attribute.getName(), rpslValue));
        }

        return new RpslObject(rpslAttributes);
    }

    public List<RpslObject> mapWhoisObjects(final Iterable<WhoisObject> whoisObjects) {
        final List<RpslObject> rpslObjects = Lists.newArrayList();
        for (WhoisObject whoisObject : whoisObjects) {
            rpslObjects.add(map(whoisObject));
        }
        return rpslObjects;
    }

    public WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return mapRpslObjects(Arrays.asList(rpslObjects));
    }

    public WhoisResources mapRpslObjects(final Iterable<RpslObject> rpslObjects) {
        final WhoisResources whoisResources = new WhoisResources();
        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (RpslObject rpslObject : rpslObjects) {
            whoisObjects.add(map(rpslObject));
        }
        whoisResources.setWhoisObjects(whoisObjects);
        return whoisResources;
    }

    public WhoisObject map(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();

        final List<Attribute> primaryKeyAttributes = new ArrayList<>();
        for (RpslAttribute keyAttribute : rpslObject.findAttributes(ObjectTemplate.getTemplate(rpslObject.getType()).getKeyAttributes())) {
            primaryKeyAttributes.add(new Attribute(keyAttribute.getKey(), keyAttribute.getCleanValue().toString()));
        }

        final List<Attribute> attributes = buildAttributes(rpslObject, source);

        return createWhoisObject(
                new Source(source),
                type,
                attributes,
                primaryKeyAttributes,
                createLink(rpslObject));
    }

    private List<Attribute> buildAttributes(RpslObject rpslObject, String source) {
        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            for (CIString value : attribute.getCleanValues()) {
                attributes.add(buildAttribute(attribute, value, source));
            }
        }
        return attributes;
    }

    abstract Attribute buildAttribute(RpslAttribute attribute, final CIString value, final String source);

    protected Link createLink(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final String key = rpslObject.getKey().toString();
        return createLink(source, type, key);
    }

    protected Link createLink(final String source, final String type, final String key) {
        return new Link("locator", String.format("%s/%s/%s/%s", baseUrl, source, type, key));
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
}
