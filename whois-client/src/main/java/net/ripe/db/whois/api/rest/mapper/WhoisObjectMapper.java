package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WhoisObjectMapper {

    private final String baseUrl;
    private final Map<Class, AttributeMapper> objectMapperFunctions;
    private final AttributeMapper primaryKeyAttributeMapper;

    @Autowired
    public WhoisObjectMapper(@Value("${api.rest.baseurl}") final String baseUrl,
                             final AttributeMapper[] objectMapperFunctions) {
        this.baseUrl = baseUrl;

        this.objectMapperFunctions = new HashMap<>(objectMapperFunctions.length);
        for (AttributeMapper objectMapperFunction : objectMapperFunctions) {
            this.objectMapperFunctions.put(objectMapperFunction.getClass(), objectMapperFunction);
        }
        this.primaryKeyAttributeMapper = this.objectMapperFunctions.get(FormattedClientAttributeMapper.class);
    }

    public RpslObject map(final WhoisObject whoisObject, final Class<?> mapFunction) {
        final List<RpslAttribute> rpslAttributes = Lists.newArrayList();
        final AttributeMapper attributeMapper = objectMapperFunctions.get(mapFunction);

        for (final Attribute attribute : whoisObject.getAttributes()) {
            rpslAttributes.addAll(attributeMapper.map(attribute));
        }

        return new RpslObject(rpslAttributes);
    }

    public List<RpslObject> mapWhoisObjects(final Iterable<WhoisObject> whoisObjects, final Class<?> mapFunction) {
        final List<RpslObject> rpslObjects = Lists.newArrayList();
        for (WhoisObject whoisObject : whoisObjects) {
            rpslObjects.add(map(whoisObject, mapFunction));
        }
        return rpslObjects;
    }

    public WhoisResources mapRpslObjects(final Class<?> mapFunction, final RpslObject... rpslObjects) {
        return mapRpslObjects(Arrays.asList(rpslObjects), mapFunction);
    }

    public WhoisResources mapRpslObjects(final Iterable<RpslObject> rpslObjects, final Class<?> mapFunction) {
        final WhoisResources whoisResources = new WhoisResources();
        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (RpslObject rpslObject : rpslObjects) {
            whoisObjects.add(map(rpslObject, mapFunction));
        }
        whoisResources.setWhoisObjects(whoisObjects);
        return whoisResources;
    }

    public WhoisResources mapRpslObjects(final Class<? extends AttributeMapper> mapFunction, final ActionRequest ... requests) {
        final WhoisResources whoisResources = new WhoisResources();
        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (ActionRequest request : requests) {
            final WhoisObject whoisObject = map(request.getRpslObject(), mapFunction);
            whoisObject.setAction(request.getAction());
            whoisObjects.add(whoisObject);
        }
        whoisResources.setWhoisObjects(whoisObjects);
        return whoisResources;
    }

    public WhoisObject map(final RpslObject rpslObject, final Class<?> mapFunction) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final AttributeMapper attributeMapper = objectMapperFunctions.get(mapFunction);

        final List<Attribute> primaryKeyAttributes = Lists.newArrayList();
        for (RpslAttribute keyAttribute : rpslObject.findAttributes(ObjectTemplate.getTemplate(rpslObject.getType()).getKeyAttributes())) {
            primaryKeyAttributes.addAll(primaryKeyAttributeMapper.map(keyAttribute, source));
        }

        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute rpslAttribute : rpslObject.getAttributes()) {
            attributes.addAll(attributeMapper.map(rpslAttribute, source));
        }

        return new WhoisObject.Builder()
            .source(new Source(source))
            .type(type)
            .attributes(attributes)
            .primaryKey(primaryKeyAttributes)
            .link(Link.create(baseUrl, rpslObject))
            .build();
    }
}
