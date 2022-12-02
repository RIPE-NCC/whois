package net.ripe.db.nrtm4;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class PayloadProcessor {

    private final String json;

    PayloadProcessor(final Object payload) {
        final ObjectMapper objectMapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
//            .annotationIntrospector(new AnnotationIntrospectorPair(
//                new JacksonAnnotationIntrospector(),
//                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())))
            .build();
        final SimpleModule module = new SimpleModule("RpslObjectSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(RpslObject.class, new RpslObjectSerializer());
        objectMapper.registerModule(module);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(df);
        try {
            this.json = objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJson() {
        return json;
    }

}
