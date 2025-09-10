package net.ripe.db.whois.api.rest.marshal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import net.ripe.db.whois.api.rest.client.StreamingException;

import java.io.IOException;
import java.io.OutputStream;

public class StreamingMarshalJson implements StreamingMarshal {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setAnnotationIntrospector(new AnnotationIntrospectorPair(
                    new JacksonAnnotationIntrospector(),
                    new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));
    }

    private final JsonGenerator generator;

    StreamingMarshalJson(OutputStream outputStream) {
        try {
            this.generator = OBJECT_MAPPER.writer(new DefaultPrettyPrinter()).createGenerator(outputStream);
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
    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void startArray(final String name) {
        try {
            generator.writeFieldName(name);
            generator.writeStartArray();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void endArray() {
        try {
            generator.writeEndArray();
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
    public void end(final String name) {
        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void writeArray(final T t) {
        try {
            generator.writeObject(t);
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
    public <T> void singleton(T t) {
        try {
            generator.writeObject(t);
            generator.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }
}
