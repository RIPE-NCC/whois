package net.ripe.db.whois.api.httpserver;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class RemoteAddrCustomizerNonTrustedTestIntegration extends AbstractIntegrationTest {


    @BeforeAll
    public static void beforeClass() {
        System.setProperty("ipranges.trusted", "172.16.0.0/12");
    }

    @AfterAll
    public static void clearProperty() {
        System.clearProperty("ipranges.trusted");
    }

    @Test
    public void help_client_ip_flag_non_trusted_ip() {
        final String index = RestTest.target(getPort(), "whois/syncupdates/TEST?HELP=yes&clientIp=10.0.0.1")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "74.125.136.99")
                .get(String.class);

        assertThat(index, containsString("From-Host: 74.125.136.99"));
    }

}
