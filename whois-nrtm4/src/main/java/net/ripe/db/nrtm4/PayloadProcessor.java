package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;


public class PayloadProcessor {

    private final String json;

    PayloadProcessor(final Object payload) {
        final ObjectMapper objectMapper = JsonMapper.builder().build();
        try {
            this.json = objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJson() {
        return json;
    }

    public String getHash() {
        // TODO: calculate hash
        return "0123456789abcdef0123456789abcdef";
    }
}
