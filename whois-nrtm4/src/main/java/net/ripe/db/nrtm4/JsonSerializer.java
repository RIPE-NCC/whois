package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Service;


@Service
public class JsonSerializer {

    private final ObjectMapper objectMapper;

    JsonSerializer() {
        objectMapper = JsonMapper.builder()
            .build();
        final SimpleModule module = new SimpleModule("RpslObjectSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(RpslObject.class, new RpslObjectSerializer());
        objectMapper.registerModule(module);
    }

    public String process(final Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
