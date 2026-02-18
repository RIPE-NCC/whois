package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.httpserver.jmx.BlockListJmx;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class BlockedPathListTestIntegration extends AbstractIntegrationTest {

    @Autowired
    QueryServer queryServer;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("whois.api.blocked.paths", "/rdap/ips/rirSearch1/rdap-bottom/2001:db8::/32, /rdap/ips/rirSearch1/rdap-up/192.0.2.0/24");
    }

    @AfterAll
    public static void clear(){ System.clearProperty("whois.api.blocked.paths"); }

    @Test
    public void blocked_path_return_forbidden() throws Exception {
        final Response response = RestTest.target(getPort(), "rdap/ips/rirSearch1/rdap-bottom/2001:db8::/32")
                .request()
                .get();

        assertThat(HttpStatus.FORBIDDEN_403, is(response.getStatus()));
        assertThat(response.readEntity(String.class), containsString("Request not allowed for policy reasons"));


        final Response response2 = RestTest.target(getPort(), "rdap/ips/rirSearch1/rdap-up/192.0.2.0/24")
                .request()
                .get();

        assertThat(HttpStatus.FORBIDDEN_403, is(response2.getStatus()));
        assertThat(response2.readEntity(String.class), containsString("Request not allowed for policy reasons"));
    }

    @Test
    public void not_blocked_path_ok() throws Exception {
        final Response response = RestTest.target(getPort(), "rdap/ips/rirSearch1/rdap-bottom/2001:db8::/35")
                .request()
                .get();

        assertThat(HttpStatus.FORBIDDEN_403, not((response.getStatus())));

        final Response response2 = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=8.8.8.8")
                .request()
                .get();

        assertThat(HttpStatus.FORBIDDEN_403, not((response.getStatus())));
    }
}
