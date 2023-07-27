package net.ripe.db.whois.api.rdap;


import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.elasticsearch.AbstractElasticSearchIntegrationTest;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public abstract class AbstractRdapIntegrationTest extends AbstractElasticSearchIntegrationTest {

    @BeforeAll
    public static void rdapSetProperties() {
        System.setProperty("rdap.sources", "TEST-GRS");
        System.setProperty("rdap.redirect.test", "https://rdap.test.net");
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
    }

    @AfterAll
    public static void rdapClearProperties() {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.test");
        System.clearProperty("rdap.public.baseUrl");
    }

    // helper methods

    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("rdap/%s", path));
    }

    protected String syncupdate(String data) {
        WebTarget resource = RestTest.target(getPort(), String.format("whois/syncupdates/test"));
        return resource.request()
                .post(javax.ws.rs.client.Entity.entity("DATA=" + RestClientUtils.encode(data),
                                MediaType.APPLICATION_FORM_URLENCODED),
                        String.class);

    }
    
    protected void assertErrorDescription(final WebApplicationException exception, final String description) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getDescription().get(0), is(description));
    }

    protected void assertErrorDescriptionContains(final WebApplicationException exception, final String description) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getDescription().get(0), containsString(description));
    }
    protected void assertErrorTitle(final WebApplicationException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), is(title));
    }

    protected void assertErrorTitleContains(final ClientErrorException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), containsString(title));
    }

    protected void assertErrorStatus(final WebApplicationException exception, final int status) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorCode(), is(status));
    }



}