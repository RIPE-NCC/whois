package net.ripe.db.whois.api;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ApiKeyTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;

    @Test
    public void no_api_key() throws Exception {
        try {
            client.resource(String.format("http://localhost:%s/api", getPort(AUDIENCE))).get(String.class);
        } catch (UniformInterfaceException e) {
            final ClientResponse response = e.getResponse();
            assertThat(response.getStatus(), is(ClientResponse.Status.FORBIDDEN.getStatusCode()));
            assertThat(response.getEntity(String.class), is("No apiKey parameter specified"));
        }
    }

    @Test
    public void invalid_api_key() throws Exception {
        try {
            client.resource(String.format("http://localhost:%s/api?apiKey=INVALID", getPort(AUDIENCE))).get(String.class);
        } catch (UniformInterfaceException e) {
            final ClientResponse response = e.getResponse();
            assertThat(response.getStatus(), is(ClientResponse.Status.FORBIDDEN.getStatusCode()));
            assertThat(response.getEntity(String.class), is("Invalid apiKey"));
        }
    }
}
