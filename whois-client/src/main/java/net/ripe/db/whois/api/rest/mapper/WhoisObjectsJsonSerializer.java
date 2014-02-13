package net.ripe.db.whois.api.rest.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisObjects;

import java.io.IOException;

public class WhoisObjectsJsonSerializer extends JsonSerializer<WhoisObjects> {

    @Override
    public void serialize(final WhoisObjects value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();

        for (WhoisObject object : value.getWhoisObjects()) {
            jgen.writeObject(object);
        }

        jgen.writeEndArray();
    }
}
