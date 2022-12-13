package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;


@Service
public class JsonSerializer {

    private final ObjectMapper objectMapper;

    JsonSerializer() {
        objectMapper = JsonMapper.builder()
            .build();
    }

    public String process(final Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
