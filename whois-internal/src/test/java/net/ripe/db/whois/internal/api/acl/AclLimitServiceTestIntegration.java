package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AclLimitServiceTestIntegration extends AbstractInternalTest {
    private static final String LIMITS_PATH = "api/acl/limits";

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclIpLimit("0/0", 1000, false);
        databaseHelper.insertAclIpLimit("::0/0", 1000, false);
        databaseHelper.insertApiKey(apiKey, "/api/acl", "acl api key");
    }

    @Test
    public void limits() throws Exception {
        databaseHelper.insertAclIpLimit("10/8", 1000, false);
        databaseHelper.insertAclIpLimit("::0/8", 1000, false);

        final List<Limit> limits = getLimits();
        assertThat(limits, hasSize(4));
        assertThat(limits.get(0).getPersonObjectLimit(), is(1000));
        assertThat(limits.get(0).isUnlimitedConnections(), is(false));
    }

    @Test
    public void getLimit_parent() throws Exception {
        final Limit limit = RestTest.target(getPort(), LIMITS_PATH, "10/8", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Limit.class);

        assertThat(limit.getPrefix(), is("0.0.0.0/0"));
    }

    @Test
    public void getLimit_exact() throws Exception {
        final Limit limit = RestTest.target(getPort(), LIMITS_PATH, "::0/0", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Limit.class);

        assertThat(limit.getPrefix(), is("::/0"));
    }

    @Test
    public void getLimit_invalid_prefix() throws Exception {
        try {
            RestTest.target(getPort(), LIMITS_PATH, "10", null, apiKey)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Limit.class);
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("'10' is not an IP string literal."));
        }
    }

    @Test
    public void createLimit() throws Exception {
        final Limit limit = RestTest.target(getPort(), LIMITS_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Limit("10.0.0.0/32", "test", 10000, true), MediaType.APPLICATION_JSON_TYPE), Limit.class);

        assertThat(limit.getPrefix(), is("10.0.0.0/32"));
        assertThat(limit.getComment(), is("test"));
        assertThat(limit.getPersonObjectLimit(), is(10000));
        assertThat(limit.isUnlimitedConnections(), is(true));

        final List<Limit> limits = getLimits();
        assertThat(limits, hasSize(3));
    }

    @Test
    public void updateLimit() throws Exception {
        final Limit limit = RestTest.target(getPort(), LIMITS_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Limit("0/0", "test", 10000, true), MediaType.APPLICATION_JSON_TYPE), Limit.class);

        assertThat(limit.getPrefix(), is("0.0.0.0/0"));
        assertThat(limit.getComment(), is("test"));
        assertThat(limit.getPersonObjectLimit(), is(10000));
        assertThat(limit.isUnlimitedConnections(), is(true));

        final List<Limit> limits = getLimits();
        assertThat(limits, hasSize(2));
    }

    @Test
    public void deleteLimit_root() throws Exception {
        try {
            RestTest.target(getPort(), LIMITS_PATH, "0/0", null, apiKey)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(Limit.class);
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Deleting the root object is not allowed"));
        }
    }

    @Test
    public void deleteLimit_unknown() throws Exception {
        try {
            RestTest.target(getPort(), LIMITS_PATH, "10.0.0.0/32", null, apiKey)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(Limit.class);
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void deleteLimit() throws Exception {
        RestTest.target(getPort(), LIMITS_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Limit("10.0.0.0/32", "test", 10000, true), MediaType.APPLICATION_JSON_TYPE));

        assertThat(getLimits(), hasSize(3));

        final Limit limit = RestTest.target(getPort(), LIMITS_PATH, "10.0.0.0/32", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete(Limit.class);

        assertThat(limit.getPrefix(), is("10.0.0.0/32"));
        assertThat(limit.getComment(), is("test"));

        assertThat(getLimits(), hasSize(2));
    }

    @SuppressWarnings("unchecked")
    private List<Limit> getLimits() {
        return RestTest.target(getPort(), LIMITS_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Limit>>() {});
    }
}
