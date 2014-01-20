package net.ripe.db.whois.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.rest.RestClientUtils;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RestTest {
    private static final Client client;

    static {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    public static final WebTarget target(final int port, final String path) {
       return client.target(String.format("http://localhost:%d/%s", port, path));
    }

    public static final WebTarget target(final int port, final String path, String queryParam, final String apiKey) {
        return client.target(String.format("http://localhost:%d/%s?%sapiKey=%s", port, path,
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                apiKey));
    }

    public static final WebTarget target(final int port, final String path, final String pathParam, String queryParam, final String apiKey) {
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
        assertEquals(text, whoisResources.getErrorMessages().get(number).getText());
        assertThat(whoisResources.getErrorMessages().get(number).getSeverity(), is(severity));
        if (argument.length > 0) {
            assertThat(whoisResources.getErrorMessages().get(number).getArgs(), hasSize(argument.length));
            for (int i = 0; i < argument.length; i++) {
                assertThat(whoisResources.getErrorMessages().get(number).getArgs().get(i).getValue(), is(argument[i]));
            }
        }
    }

    public static void assertErrorCount(final WhoisResources whoisResources, final int count) {
        assertThat(whoisResources.getErrorMessages(), hasSize(count));
    }
}
