package net.ripe.db.whois.api.acl;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AclLimitServiceTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;
    private static final String LIMITS_PATH = "api/acl/limits";

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclIpLimit("0/0", 1000, false);
        databaseHelper.insertAclIpLimit("::0/0", 1000, false);
        databaseHelper.insertApiKey("DB-WHOIS-testapikey", "/api/acl", "acl api key");
        setApiKey("DB-WHOIS-testapikey");
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
        final Limit limit = createResource(AUDIENCE, LIMITS_PATH, "10/8")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Limit.class);

        assertThat(limit.getPrefix(), is("0/0"));
    }

    @Test
    public void getLimit_exact() throws Exception {
        final Limit limit = createResource(AUDIENCE, LIMITS_PATH, "::0/0")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Limit.class);

        assertThat(limit.getPrefix(), is("::0/0"));
    }

    @Test
    public void getLimit_invalid_prefix() throws Exception {
        try {
            createResource(AUDIENCE, LIMITS_PATH, "10")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Limit.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("'10' is not an IP string literal."));
        }
    }

    @Test
    public void createLimit() throws Exception {
        final Limit limit = createResource(AUDIENCE, LIMITS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Limit.class, new Limit("10.0.0.0/32", "test", 10000, true));

        assertThat(limit.getPrefix(), is("10.0.0.0/32"));
        assertThat(limit.getComment(), is("test"));
        assertThat(limit.getPersonObjectLimit(), is(10000));
        assertThat(limit.isUnlimitedConnections(), is(true));

        final List<Limit> limits = getLimits();
        assertThat(limits, hasSize(3));
    }

    @Test
    public void updateLimit() throws Exception {
        final Limit limit = createResource(AUDIENCE, LIMITS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Limit.class, new Limit("0/0", "test", 10000, true));

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
            createResource(AUDIENCE, LIMITS_PATH, "0/0")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete(Limit.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Deleting the root object is not allowed"));
        }
    }

    @Test
    public void deleteLimit_unknown() throws Exception {
        try {
            createResource(AUDIENCE, LIMITS_PATH, "10.0.0.0/32")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete(Limit.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void deleteLimit() throws Exception {
        createResource(AUDIENCE, LIMITS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Limit.class, new Limit("10.0.0.0/32", "test", 10000, true));

        assertThat(getLimits(), hasSize(3));

        final Limit limit = createResource(AUDIENCE, LIMITS_PATH, "10.0.0.0/32")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(Limit.class);

        assertThat(limit.getPrefix(), is("10.0.0.0/32"));
        assertThat(limit.getComment(), is("test"));

        assertThat(getLimits(), hasSize(2));
    }

    @SuppressWarnings("unchecked")
    private List<Limit> getLimits() {
        return createResource(AUDIENCE, LIMITS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Limit>>() {
                });
    }
}
