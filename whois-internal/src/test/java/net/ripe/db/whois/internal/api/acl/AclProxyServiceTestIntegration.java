package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
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
public class AclProxyServiceTestIntegration extends AbstractInternalTest {
    private static final String PROXIES_PATH = "api/acl/proxies";

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclIpProxy("10.0.0.0/32");
        databaseHelper.insertApiKey(apiKey, "/api/acl", "acl api key");
    }

    @Test
    public void proxies() throws Exception {
        databaseHelper.insertAclIpProxy("10.0.0.1/32");
        databaseHelper.insertAclIpProxy("10.0.0.2/32");
        databaseHelper.insertAclIpProxy("10.0.0.3/32");

        assertThat(getProxies(), hasSize(4));
    }

    @Test
    public void getProxy() throws Exception {
        final Proxy proxy = RestTest.target(getPort(), PROXIES_PATH, "10.0.0.0/32", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
    }

    @Test
    public void createProxy() throws Exception {
        final Proxy proxy = RestTest.target(getPort(), PROXIES_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Proxy("10.0.0.1/32", "test"), MediaType.APPLICATION_JSON_TYPE), Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.1/32"));
        assertThat(proxy.getComment(), is("test"));

        assertThat(getProxies(), hasSize(2));
    }

    @Test
    public void updateProxy() throws Exception {
        final Proxy proxy = RestTest.target(getPort(), PROXIES_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new Proxy("10.0.0.0/32", "test"), MediaType.APPLICATION_JSON_TYPE), Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
        assertThat(proxy.getComment(), is("test"));

        assertThat(getProxies(), hasSize(1));
    }

    @Test
    public void deleteProxy() throws Exception {
        final Proxy proxy = RestTest.target(getPort(), PROXIES_PATH, "10.0.0.0/32", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete(Proxy.class);

        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));

        assertThat(getProxies(), hasSize(0));
    }

    @Test
    public void deleteProxyUnencodedPrefix() throws Exception {
        databaseHelper.insertAclIpProxy("2a01:488:67:1000::/64");

        RestTest.target(getPort(), PROXIES_PATH + "/2a01:488:67:1000::/64", null, apiKey).request().delete();

        assertThat(getProxies(), hasSize(1));
    }

    @Test
    public void deleteProxyUnencodedPrefixWithExtension() throws Exception {
        databaseHelper.insertAclIpProxy("2a01:488:67:1000::/64");

        RestTest.target(getPort(), PROXIES_PATH + "/2a01:488:67:1000::/64.json", null, apiKey).request().delete();

        assertThat(getProxies(), hasSize(1));
    }

    @SuppressWarnings("unchecked")
    private List<Proxy> getProxies() {
        return RestTest.target(getPort(), PROXIES_PATH, null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Proxy>>() {});
    }
}
