package net.ripe.db.whois.api.whois;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.OutputStream;

class StreamingMarshalJson implements StreamingMarshal {
    private static JsonFactory jsonFactory;

    static {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setAnnotationIntrospector(new AnnotationIntrospector.Pair(
                new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector()));

        jsonFactory = objectMapper.getJsonFactory();
    }

    private JsonGenerator generator;

    @Override
    public void open(final OutputStream outputStream, final String... parentElementNames) {
        try {
            generator = jsonFactory.createJsonGenerator(outputStream);
            generator.writeStartObject();

            for (final String parentElementName : parentElementNames) {
                generator.writeObjectFieldStart(parentElementName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Open", e);
        }
    }

    @Override
    public <T> void write(final String name, final T t) {
        try {
            generator.writeObjectField(name, t);
        } catch (IOException e) {
            throw new RuntimeException("Write", e);
        }
    }

    @Override
    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            throw new RuntimeException("Close", e);
        }
    }
}
