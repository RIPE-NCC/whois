package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.collect.Lists;
import net.ripe.db.whois.update.dns.zonemaster.domain.Request;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class RequestTest {

    private static final JsonFactory factory;

    static {
        final ObjectMapper objectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)      // TODO: empty ds_info and nameservers arrays
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(
                new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));

         factory = objectMapper.getFactory();
    }

    @Test
    public void test() throws Exception {
        final Request request = new Request();
        request.setMethod(Request.Method.START_DOMAIN_TEST);
        final Request.Params params = new Request.Params();
        params.setDsInfos(Lists.newArrayList());
        params.setNameservers(Lists.newArrayList());
        request.setParams(params);

        final String requestString = toJson(request);

        assertThat(requestString, containsString("\"dsInfos\" : [ ],"));
        assertThat(requestString, containsString("\"nameservers\" : [ ],"));
    }


    private String toJson(final Object object) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (JsonGenerator generator = factory.createGenerator(baos)) {
            generator.writeObject(object);
        }

        return new String(baos.toByteArray());
    }

}
