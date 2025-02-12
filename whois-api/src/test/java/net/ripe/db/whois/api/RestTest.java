package net.ripe.db.whois.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class RestTest {
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
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    public static WebTarget target(final String fullPath) {
        return client.target(fullPath);
    }

    public static WebTarget target(final int port, final String path) {
        return client.target(String.format("http://localhost:%d/%s", port, path));
    }

    public static WebTarget target(final int port, final String path, String queryParam) {
        return client.target(String.format("http://localhost:%d/%s?%s", port, path,
                StringUtils.isBlank(queryParam) ? "" : queryParam));
    }

    public static WebTarget target(final int port, final String path, String queryParam, final String apiKey) {
        return client.target(String.format("http://localhost:%d/%s?%sapiKey=%s", port, path,
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                apiKey));
    }

    public static WebTarget target(final int port, final String path, final String pathParam, String queryParam, final String apiKey) {
        return client.target(String.format("http://localhost:%d/%s/%s?%sapiKey=%s", port, path, RestClientUtils.encode(pathParam),
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                apiKey));
    }

    public static WhoisResources mapClientException(final ClientErrorException e) {
        return e.getResponse().readEntity(WhoisResources.class);
    }

    public static void assertOnlyErrorMessage(final ClientErrorException e, final String severity, final String text, final String... argument) {
        WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
        assertErrorCount(whoisResources, 1);
        assertErrorMessage(whoisResources, 0, severity, text, argument);
    }

    public static void assertErrorMessage(final ClientErrorException e, final int number, final String severity, final String text, final String... argument) {
        WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
        assertErrorMessage(whoisResources, number, severity, text, argument);
    }

    public static void assertErrorMessage(final WhoisResources whoisResources, final int number, final String severity, final String text, final String... argument) {

        ErrorMessage errorMsg = whoisResources.getErrorMessages().get(number);

        assertThat(errorMsg.getText(), is(text));
        assertThat(errorMsg.getSeverity(), is(severity));

        if (argument.length > 0) {
            assertThat(errorMsg.getArgs(), hasSize(argument.length));
            for (int i = 0; i < argument.length; i++) {
                assertThat(errorMsg.getArgs().get(i).getValue(), is(argument[i]));
            }
        }
    }

    public static void assertInfoCount(final WhoisResources whoisResources, final int expectedCount) {
        int errorCount = 0;
        for (ErrorMessage em : whoisResources.getErrorMessages()) {
            if ("Info".equalsIgnoreCase(em.getSeverity())) {
                errorCount++;
            }
        }
        assertThat(errorCount, is(expectedCount));
    }

    public static void assertWarningCount(final WhoisResources whoisResources, final int expectedCount) {
        int errorCount = 0;
        for (ErrorMessage em : whoisResources.getErrorMessages()) {
            if ("Warning".equalsIgnoreCase(em.getSeverity())) {
                errorCount++;
            }
        }
        assertThat(errorCount, is(expectedCount));
    }

    public static void assertErrorCount(final WhoisResources whoisResources, final int expectedCount) {
        int errorCount = 0;
        for (ErrorMessage em : whoisResources.getErrorMessages()) {
            if ("Error".equalsIgnoreCase(em.getSeverity())) {
                errorCount++;
            }
        }
        assertThat(errorCount, is(expectedCount));
    }
}
