package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        objectMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(
                new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));

        jsonFactory = objectMapper.getFactory();
    }

    private JsonGenerator generator;

    @Override
    public void open(final OutputStream outputStream, String ignore) {
        try {
            generator = jsonFactory.createGenerator(outputStream);
            generator.writeStartObject();
            // json document has a natural root, ignore arg
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

    public void startArray(final String name) {
        try {
            generator.writeArrayFieldStart(name);
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

    public void endArray() {
        try {
            generator.writeEndArray();
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

    public <T> void writeArray(final T t) {
        try {
            generator.writeObject(t);
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
