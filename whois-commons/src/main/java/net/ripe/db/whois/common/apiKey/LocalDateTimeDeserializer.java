package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        final ZonedDateTime utcZonedDateTime = ZonedDateTime.parse(dateStr);
        return utcZonedDateTime.toLocalDateTime();
    }
}
