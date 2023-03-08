package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;


public class RpslObjectDeserializer extends JsonDeserializer<RpslObject> {

    @Override
    public RpslObject deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JacksonException {
        return RpslObject.parse(jsonParser.getText());
    }

}
