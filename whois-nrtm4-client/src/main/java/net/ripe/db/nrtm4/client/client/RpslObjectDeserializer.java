package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;

public class RpslObjectDeserializer extends JsonDeserializer<RpslObject>{

    @Override
    public RpslObject deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        return RpslObject.parse(jsonParser.getText());
    }

}
