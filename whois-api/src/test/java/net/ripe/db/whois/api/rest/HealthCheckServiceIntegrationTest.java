package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Category(IntegrationTest.class)
public class HealthCheckServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    RestClient restClient;

    @Autowired
    HealthCheckService service;

    @Test
    public void testHealthyLBEnabled() {
        service.setLoadbalancerEnabled(true);
        Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.readEntity(String.class), is("OK"));
    }

    @Test
    public void testHealthyLBDisabled() {
        service.setLoadbalancerEnabled(false);
        Response response = RestTest.target(getPort(), "whois/healthcheck")
                .request()
                .get(Response.class);

        assertThat(response.getStatus(), is(Status.SERVICE_UNAVAILABLE.getStatusCode()));
        assertThat(response.readEntity(String.class), is("DISABLED"));
    }

}
