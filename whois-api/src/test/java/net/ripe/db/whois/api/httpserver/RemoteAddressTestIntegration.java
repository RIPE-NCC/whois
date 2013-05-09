package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import org.eclipse.jetty.http.HttpHeaders;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class RemoteAddressTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Autowired IpRanges ipRanges;

    @Test
    public void help_localhost() throws Exception {
        final String index = client.resource(
                String.format("http://localhost:%s/whois/syncupdates/TEST?HELP=yes", getPort(AUDIENCE)))
                .get(String.class);

        assertThat(index, containsString("From-Host: 127.0.0.1"));
    }

    @Test
    public void help_forward_header() throws Exception {
        final String index = client.resource(
                String.format("http://localhost:%s/whois/syncupdates/TEST?HELP=yes", getPort(AUDIENCE)))
                .header(HttpHeaders.X_FORWARDED_FOR, "10.0.0.0")
                .get(String.class);

        assertThat(index, containsString("From-Host: 10.0.0.0"));
    }

    @Test
    public void help_forward_header_ripe() throws Exception {
        ipRanges.setTrusted("193/8");

        final String index = client.resource(
                String.format("http://localhost:%s/whois/syncupdates/TEST?HELP=yes", getPort(AUDIENCE)))
                .header(HttpHeaders.X_FORWARDED_FOR, "193.0.20.1")
                .header(HttpHeaders.X_FORWARDED_FOR, "74.125.136.99")
                .get(String.class);

        assertThat(index, containsString("From-Host: 74.125.136.99"));
    }

    @Test
    public void rdapServerTest() throws Exception {
        Thread.sleep(1500);
    }
}
