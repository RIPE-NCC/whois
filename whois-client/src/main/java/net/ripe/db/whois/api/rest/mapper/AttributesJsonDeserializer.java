package net.ripe.db.whois.api.rest.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Attributes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class AttributesJsonDeserializer extends JsonDeserializer<Attributes> {

    @Override
    public Attributes deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        jp.nextToken();

        final Iterator<Attribute> iterator = jp.readValuesAs(Attribute.class);

        final List<Attribute> attributesList = Lists.newArrayList();
        while (iterator.hasNext()) {
            attributesList.add(iterator.next());
        }

        return new Attributes(attributesList);
    }
}
