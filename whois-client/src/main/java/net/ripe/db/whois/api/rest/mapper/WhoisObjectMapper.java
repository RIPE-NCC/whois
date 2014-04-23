package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WhoisObjectMapper {

    protected final String baseUrl;
    protected final Map<Class, AttributeMapper> objectMapperFunctions;

    @Autowired
    public WhoisObjectMapper(@Value("${api.rest.baseurl}") final String baseUrl,
                             AttributeMapper[] objectMapperFunctions) {
        this.baseUrl = baseUrl;

        this.objectMapperFunctions = new HashMap<>(objectMapperFunctions.length);
        for (AttributeMapper objectMapperFunction : objectMapperFunctions) {
            this.objectMapperFunctions.put(objectMapperFunction.getClass(), objectMapperFunction);
        }
    }

    public RpslObject map(final WhoisObject whoisObject, Class<?> mapFunction) {
        final List<RpslAttribute> rpslAttributes = Lists.newArrayList();
        final AttributeMapper attributeMapper = objectMapperFunctions.get(mapFunction);

        for (final Attribute attribute : whoisObject.getAttributes()) {
            rpslAttributes.addAll(attributeMapper.map(attribute));
        }

        return new RpslObject(rpslAttributes);
    }

    public List<RpslObject> mapWhoisObjects(final Iterable<WhoisObject> whoisObjects, Class<?> mapFunction) {
        final List<RpslObject> rpslObjects = Lists.newArrayList();
        for (WhoisObject whoisObject : whoisObjects) {
            rpslObjects.add(map(whoisObject, mapFunction));
        }
        return rpslObjects;
    }

    public WhoisResources mapRpslObjects(Class<?> mapFunction, final RpslObject... rpslObjects) {
        return mapRpslObjects(Arrays.asList(rpslObjects), mapFunction);
    }

    public WhoisResources mapRpslObjects(final Iterable<RpslObject> rpslObjects, Class<?> mapFunction) {
        final WhoisResources whoisResources = new WhoisResources();
        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (RpslObject rpslObject : rpslObjects) {
            whoisObjects.add(map(rpslObject, mapFunction));
        }
        whoisResources.setWhoisObjects(whoisObjects);
        return whoisResources;
    }

    public WhoisObject map(final RpslObject rpslObject, Class<?> mapFunction) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final AttributeMapper attributeMapper = objectMapperFunctions.get(mapFunction);

        final List<Attribute> primaryKeyAttributes = new ArrayList<>();
        for (RpslAttribute keyAttribute : rpslObject.findAttributes(ObjectTemplate.getTemplate(rpslObject.getType()).getKeyAttributes())) {
            primaryKeyAttributes.addAll(attributeMapper.map(keyAttribute, source));
        }

        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute rpslAttribute : rpslObject.getAttributes()) {
            attributes.addAll(attributeMapper.map(rpslAttribute, source));
        }

        return createWhoisObject(
                new Source(source),
                type,
                attributes,
                primaryKeyAttributes,
                createLink(rpslObject));
    }

    protected Link createLink(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final String key = rpslObject.getKey().toString();
        return createLink(source, type, key);
    }

    // TODO: duplicate method
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
