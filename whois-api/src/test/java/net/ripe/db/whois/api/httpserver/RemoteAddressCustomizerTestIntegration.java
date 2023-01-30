package net.ripe.db.whois.api.httpserver;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;

import net.ripe.db.whois.common.domain.IpRanges;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("IntegrationTest")
public class RemoteAddressCustomizerTestIntegration extends AbstractIntegrationTest {
    @Autowired IpRanges ipRanges;

    @Test
    public void help_localhost() throws Exception {
        final String index = RestTest.target(getPort(), "whois/syncupdates/TEST?HELP=yes")
                .request()
                .get(String.class);

        assertThat(index, containsString("From-Host: 127.0.0.1"));
    }

    @Test
    public void help_forward_header() throws Exception {
        final String index = RestTest.target(getPort(), "whois/syncupdates/TEST?HELP=yes")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.0.0.0")
                .get(String.class);

        assertThat(index, containsString("From-Host: 10.0.0.0"));
    }

    @Test
    public void help_forward_header_ripe() throws Exception {
        ipRanges.setTrusted("193/8");

        final String index = RestTest.target(getPort(), "whois/syncupdates/TEST?HELP=yes")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "193.0.20.1")
                .header(HttpHeaders.X_FORWARDED_FOR, "74.125.136.99")
                .get(String.class);

        assertThat(index, containsString("From-Host: 74.125.136.99"));
    }
}
