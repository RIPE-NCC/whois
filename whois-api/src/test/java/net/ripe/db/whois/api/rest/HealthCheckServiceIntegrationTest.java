package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.common.ReadinessUpdater;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class HealthCheckServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    RestClient restClient;

    @Autowired
    ReadinessUpdater readinessUpdater;

    @Test
    public void testHealthyLBEnabled() {
        readinessUpdater.up();
        Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.readEntity(String.class), is("OK"));
    }

    @Test
    public void testHealthyLBDisabled() {
        readinessUpdater.down();
        Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Status.SERVICE_UNAVAILABLE.getStatusCode()));
        assertThat(response.readEntity(String.class), is("DISABLED"));
    }

}
