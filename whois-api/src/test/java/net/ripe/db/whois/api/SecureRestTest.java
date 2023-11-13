package net.ripe.db.whois.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class SecureRestTest extends RestTest {

    private static final Client client;
    static {
        final ObjectMapper objectMapper = JsonMapper.builder().build();
        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));

        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider()
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jsonProvider.setMapper(objectMapper);

        client = ClientBuilder.newBuilder()
                .sslContext(RestClientUtils.trustAllSSLContext())
                .hostnameVerifier((hostname, session) -> true)
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    public static WebTarget target(final int port, final String path) {
        return client.target(String.format("https://localhost:%d/%s", port, path));
    }


}
