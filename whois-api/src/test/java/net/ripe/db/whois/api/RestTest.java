package net.ripe.db.whois.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
                encode(apiKey));
        return client.target(format);
    }

    public static final WebTarget target(final int port, final String path, final String pathParam, String queryParam, final String apiKey) {
        return client.target(String.format("http://localhost:%d/%s/%s?%sapiKey=%s", port, path, encode(pathParam),
                StringUtils.isBlank(queryParam) ? "" : queryParam + "&",
                encode(apiKey)));
    }

    public static final String encode(final String param) {
        return encode(param, "UTF-8");
    }

    public static final String encode(final String param, final String encoding) {
        try {
            return URLEncoder.encode(param, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
