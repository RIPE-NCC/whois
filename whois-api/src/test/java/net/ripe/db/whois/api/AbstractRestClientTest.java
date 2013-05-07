package net.ripe.db.whois.api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.acl.ApiKeyFilter;
import net.ripe.db.whois.api.httpserver.Audience;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class AbstractRestClientTest extends AbstractIntegrationTest {
    @Autowired protected ApiKeyFilter apiKeyFilter;

    protected Client client;

    @BeforeClass
    public static void setupKey() throws IOException {
        System.setProperty("api.key", "DB-RIPE-ZwBAFuR5JuBxQCnQ");
    }

    @AfterClass
    public static void cleanupKey() throws IOException {
        System.clearProperty("api.key");
    }

    @Before
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJaxbJsonProvider.class);
        client = Client.create(cc);
    }

    protected WebResource createStaticResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }

    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s?apiKey=%s", getPort(audience), path, apiKeyFilter.getApiKey()));
    }

    protected WebResource createResource(final Audience audience, final String path, final String param) {
        return client.resource(String.format("http://localhost:%s/%s/%s?apiKey=%s", getPort(audience), path, encode(param), apiKeyFilter.getApiKey()));
    }

    protected String encode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
