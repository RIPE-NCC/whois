package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AclProxyServiceTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;
    private static final String PROXIES_PATH = "api/acl/proxies";

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclIpProxy("10.0.0.0/32");
        databaseHelper.insertApiKey("DB-WHOIS-testapikey", "/api/acl", "acl api key");
        setApiKey("DB-WHOIS-testapikey");
    }

    @Test
    public void proxies() throws Exception {
        databaseHelper.insertAclIpProxy("10.0.0.1/32");
        databaseHelper.insertAclIpProxy("10.0.0.2/32");
        databaseHelper.insertAclIpProxy("10.0.0.3/32");

        @SuppressWarnings("unchecked")
        final List<Proxy> proxies = getProxies();

        assertThat(proxies, hasSize(4));
    }

    @Test
    public void getProxy() throws Exception {
        final Proxy proxy = createResource(AUDIENCE, PROXIES_PATH, "10.0.0.0/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
    }

    @Test
    public void createProxy() throws Exception {
        final Proxy proxy = createResource(AUDIENCE, PROXIES_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Proxy("10.0.0.1/32", "test"), MediaType.APPLICATION_JSON_TYPE), Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.1/32"));
        assertThat(proxy.getComment(), is("test"));

        final List<Proxy> proxies = getProxies();
        assertThat(proxies, hasSize(2));
    }

    @Test
    public void updateProxy() throws Exception {
        final Proxy proxy = createResource(AUDIENCE, PROXIES_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Proxy("10.0.0.0/32", "test"), MediaType.APPLICATION_JSON_TYPE), Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
        assertThat(proxy.getComment(), is("test"));

        final List<Proxy> proxies = getProxies();
        assertThat(proxies, hasSize(1));
    }

    @Test
    public void deleteProxy() throws Exception {
        final Proxy proxy = createResource(AUDIENCE, PROXIES_PATH, "10.0.0.0/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete(Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));

        final List<Proxy> proxies = getProxies();
        assertThat(proxies, hasSize(0));
    }

    @SuppressWarnings("unchecked")
    private List<Proxy> getProxies() {
        return createResource(AUDIENCE, PROXIES_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Proxy>>() {});
    }
}
