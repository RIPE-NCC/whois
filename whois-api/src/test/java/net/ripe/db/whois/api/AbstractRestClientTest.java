package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.Audience;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public abstract class AbstractRestClientTest extends AbstractIntegrationTest {
    protected Client client;

    @Value("${api.key}")
    private String apiKey;

    @Before
    public void setUpClient() throws Exception {
        this.client = ClientBuilder.newBuilder()
                .register(JacksonJaxbJsonProvider.class)
                .build();
    }

    protected WebTarget createStaticResource(final Audience audience, final String path) {
       return client.target(String.format("http://localhost:%s/%s", getPort(audience), path));
    }

    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%s/%s?apiKey=%s", getPort(audience), path, apiKey));
    }

    protected WebTarget createResourceGet(final Audience audience, final String pathAndParams) {
        return client.target(String.format("http://localhost:%s/%s&apiKey=%s", getPort(audience), pathAndParams, apiKey));
    }

    protected WebTarget createResource(final Audience audience, final String path, final String param) {
        return client.target(String.format("http://localhost:%s/%s/%s?apiKey=%s", getPort(audience), path, encode(param), apiKey));
    }

    protected String encode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
