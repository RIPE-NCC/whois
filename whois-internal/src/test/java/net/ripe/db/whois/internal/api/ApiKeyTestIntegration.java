package net.ripe.db.whois.internal.api;

import net.ripe.db.whois.api.RestClient;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ForbiddenException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class ApiKeyTestIntegration extends AbstractInternalTest {

    @Test
    public void no_api_key() {
        try {
            RestClient.target(getPort(), "api").request().get(String.class);
            fail();
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().readEntity(String.class), is("No apiKey parameter specified"));
        }
    }

    @Test
    public void invalid_api_key() {
        try {
            RestClient.target(getPort(), "api", null, "INVALID").request().get(String.class);
            fail();
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Invalid apiKey"));
        }
    }
}
