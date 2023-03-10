package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;


public class RpslObjectJsonSupport {

    static class Serializer extends JsonSerializer<RpslObject> {

        @Override
        public void serialize(final RpslObject rpslObject, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(rpslObject.toString());
        }

    }

    static class Deserializer extends JsonDeserializer<RpslObject> {

        @Override
        public RpslObject deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JacksonException {
            return RpslObject.parse(jsonParser.getText());
        }

    }

}
