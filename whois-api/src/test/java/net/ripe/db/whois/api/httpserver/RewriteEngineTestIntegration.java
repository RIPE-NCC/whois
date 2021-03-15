package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class RewriteEngineTestIntegration extends AbstractIntegrationTest {

    @BeforeClass
    public static void enableRewriteEngine() {
        System.setProperty("rewrite.engine.enabled", "true");
    }

    @AfterClass
    public static void disableRewriteEngine() {
        System.clearProperty("rewrite.engine.enabled");
    }

    @Value("${api.rest.baseurl}")
    private String restApiBaseUrl;

    @Before
    public void setup() {
        databaseHelper.addObject(
            "person: Test Person\n" +
             "nic-hdl: TP1-TEST\n" +
             "source: TEST");
    }

    @Test
    public void rest_lookup_with_rewrite() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "test/person/TP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void rdap_lookup_with_rewrite() {
        final Entity person = RestTest.target(getPort(), "entity/TP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "rdap"))
                .get(Entity.class);

        assertThat(person.getHandle(), is("TP1-TEST"));
    }

    @Test
    public void syncupdates_with_rewrite() {
        final Response response = RestTest.target(getPort(), "?HELP=yes")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "syncupdates"))
                .get(Response.class);

        final String responseBody = response.readEntity(String.class);
        assertThat(responseBody, containsString("You have requested Help information from the RIPE NCC Database"));

    }

    // helper methods

    private String getHost(final String url) {
        final URI uri = URI.create(url);
        return uri.getHost();
    }


}
