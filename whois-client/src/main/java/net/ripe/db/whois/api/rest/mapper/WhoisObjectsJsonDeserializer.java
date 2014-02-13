package net.ripe.db.whois.api.rest.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisObjects;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class WhoisObjectsJsonDeserializer extends JsonDeserializer<WhoisObjects> {

    @Override
    public WhoisObjects deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {

        // get past "objects ["
        jp.nextToken();

        final Iterator<WhoisObject> whoisObjectIterator = jp.readValuesAs(WhoisObject.class);

        final List<WhoisObject> whoisObjectList = Lists.newArrayList();
        while (whoisObjectIterator.hasNext()) {
            whoisObjectList.add(whoisObjectIterator.next());
        }

        return new WhoisObjects(whoisObjectList);
    }
}
