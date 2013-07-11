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
    protected static JsonFactory jsonFactory;

    static {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setAnnotationIntrospector(new AnnotationIntrospector.Pair(
                new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector()));

        jsonFactory = objectMapper.getJsonFactory();
    }

    protected JsonGenerator generator;

    @Override
    public void open(final OutputStream outputStream) {
        try {
            generator = jsonFactory.createJsonGenerator(outputStream);
            generator.writeStartObject();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void start(final String name) {
        try {
            generator.writeObjectFieldStart(name);
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void end() {
        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void write(final String name, final T t) {
        try {
            generator.writeObjectField(name, t);
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void writeRaw(final String str) {
        return;
    }

    @Override
    public void writeObject(final Object o) {
        return;
    }

    @Override
    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }
}
