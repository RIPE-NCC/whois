package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.common.ReadinessHealthCheck;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class HealthCheckServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    RestClient restClient;

    @Autowired ReadinessHealthCheck readinessHealthCheck;

    @Test
    public void loadbalancerEnabledWhenRunning() {
        readinessHealthCheck.up();

        final Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.readEntity(String.class), is("OK"));
    }

    @Test
    public void loadbalancerDisabledWhenShutdown() {
        readinessHealthCheck.down();

        final Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Response.Status.SERVICE_UNAVAILABLE.getStatusCode()));
        assertThat(response.readEntity(String.class), is("DISABLED"));
    }

}
