package net.ripe.db.whois.api.rest.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Attributes;

import java.io.IOException;

public class AttributesJsonSerializer extends JsonSerializer<Attributes> {
    @Override
    public void serialize(final Attributes value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();

        for (Attribute attribute : value.getAttributes()) {
            jgen.writeObject(attribute);
        }

        jgen.writeEndArray();
    }
}
