package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.OutputStream;

class StreamingMarshalJson implements StreamingMarshal {
    private static JsonFactory jsonFactory;

    static {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        objectMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(
                new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));

        jsonFactory = objectMapper.getFactory();
    }

    private final JsonGenerator generator;

    StreamingMarshalJson(OutputStream outputStream) {
        try {
            generator = jsonFactory.createGenerator(outputStream);
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void open() {
        try {
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
    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }
}
