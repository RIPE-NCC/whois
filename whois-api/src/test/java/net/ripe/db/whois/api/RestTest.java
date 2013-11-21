package net.ripe.db.whois.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.rest.RestClientUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

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
        final String format = String.format("http://localhost:%d/%s?%sapiKey=%s", port, path,
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                RestClientUtils.encode(apiKey));
        return client.target(RestClientUtils.encode(format));
    }

    public static final WebTarget target(final int port, final String path, final String pathParam, String queryParam, final String apiKey) {
        return client.target(String.format("http://localhost:%d/%s/%s?%sapiKey=%s", port, path, RestClientUtils.encode(pathParam),
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                RestClientUtils.encode(apiKey)));
    }
}
