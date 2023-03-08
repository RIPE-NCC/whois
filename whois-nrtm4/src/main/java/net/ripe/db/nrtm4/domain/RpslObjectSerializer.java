package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;


public class RpslObjectSerializer extends JsonSerializer<RpslObject> {

    @Override
    public void serialize(final RpslObject rpslObject, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(rpslObject.toString());
    }

}
