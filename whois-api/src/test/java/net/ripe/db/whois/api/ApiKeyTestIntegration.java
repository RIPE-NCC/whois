package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ApiKeyTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;

    @Test
    public void no_api_key() {
        try {
            client.target(String.format("http://localhost:%s/api", getPort(AUDIENCE))).request().get(String.class);
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
            assertThat(e.getResponse().readEntity(String.class), is("No apiKey parameter specified"));
        }
    }

    @Test
    public void invalid_api_key() {
        try {
            client.target(String.format("http://localhost:%s/api?apiKey=INVALID", getPort(AUDIENCE))).request().get(String.class);
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
            assertThat(e.getResponse().readEntity(String.class), is("Invalid apiKey"));
        }
    }
}
