package net.ripe.db.whois.api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.httpserver.Audience;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Before;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class AbstractRestClientTest extends AbstractIntegrationTest {
    protected Client client;

    protected String apiKey;

    @Before
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJaxbJsonProvider.class);
        client = Client.create(cc);
    }

    protected void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    protected WebResource createStaticResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }

    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s?apiKey=%s", getPort(audience), path, apiKey));
    }

    protected WebResource createResourceGet(final Audience audience, final String pathAndParams) {
        return client.resource(String.format("http://localhost:%s/%s&apiKey=%s", getPort(audience), pathAndParams, apiKey));
    }

    protected WebResource createResource(final Audience audience, final String path, final String param) {
        return client.resource(String.format("http://localhost:%s/%s/%s?apiKey=%s", getPort(audience), path, encode(param), apiKey));
    }

    protected String encode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
