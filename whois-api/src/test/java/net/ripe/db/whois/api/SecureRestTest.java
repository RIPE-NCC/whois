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

import javax.net.ssl.SSLContext;

/**
 * REST client that uses HTTPS.
 *
 * Only used for testing because we need to connect to "localhost", so
 * hostname verification (SNI) is turned off and self-signed certificates are trusted.
 *
 */
public class SecureRestTest extends RestTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    static {
        OBJECT_MAPPER.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));
    }

    private static final JacksonJsonProvider JSON_PROVIDER = new JacksonJsonProvider();
    static {
        JSON_PROVIDER.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        JSON_PROVIDER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        JSON_PROVIDER.setMapper(OBJECT_MAPPER);
    }

    public static WebTarget target(final int port, final String path) {
        return client().target(String.format("https://localhost:%d/%s", port, path));
    }

    public static WebTarget target(final SSLContext sslContext, final int port, final String path) {
        return client(sslContext).target(String.format("https://localhost:%d/%s", port, path));
    }

    private static Client client() {
        return client(RestClientUtils.trustAllSSLContext());
    }

    private static Client client(final SSLContext sslContext) {
        return ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .hostnameVerifier((hostname, session) -> true)
                .register(MultiPartFeature.class)
                .register(JSON_PROVIDER)
                .build();
    }
}
