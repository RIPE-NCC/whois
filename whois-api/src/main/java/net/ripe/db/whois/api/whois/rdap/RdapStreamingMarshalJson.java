package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.StreamingException;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class RdapStreamingMarshalJson implements StreamingMarshal {
    protected static JsonFactory jsonFactory;

    static {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospector.Pair(
                        new JacksonAnnotationIntrospector(),
                        new JaxbAnnotationIntrospector()));

        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        objectMapper.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, true);

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");     // TODO: use Joda time
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(df);
        jsonFactory = objectMapper.getJsonFactory();
    }

    protected JsonGenerator generator;

    @Override
    public void open(final OutputStream outputStream) {
        try {
            generator = jsonFactory.createJsonGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void start(final String name) {
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
    public <T> void writeObject(final T t) {
        try {
            generator.writeObject(t);
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void writeRaw(final String str) {
        try {
            generator.writeRaw(str);
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
