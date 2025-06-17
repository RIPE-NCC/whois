package net.ripe.db.whois.rdap;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.rdap.domain.Ip;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RdapRedirectTestIntegration extends AbstractIntegrationTest {

    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DailySchedulerDao dailySchedulerDao;

    @BeforeAll
    public static void setProperties() throws IOException {
        System.setProperty("rdap.sources", "ONE-GRS,TWO-GRS,THREE-GRS");
        System.setProperty("rdap.redirect.one", "https://rdap.one.net");
        System.setProperty("rdap.redirect.two", "");
        // no property set for three-grs
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
        DatabaseHelper.addGrsDatabases("ONE-GRS", "TWO-GRS", "THREE-GRS");
    }

    @AfterAll
    public static void clearProperties() throws IOException {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.one");
        System.clearProperty("rdap.redirect.two");
        System.clearProperty("rdap.public.baseUrl");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("" +
                "inetnum:       0.0.0.0 - 255.255.255.255\n" +
                "netname:       IANA-BLK\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inet6num:      ::/0\n" +
                "netname:       IANA-BLK\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        deleteResourceData("test", "0.0.0.0/0");
        deleteResourceData("test", "::/0");

        addResourceData("one", "AS100");
        addResourceData("two", "AS200");
        addResourceData("three", "AS300");

        addResourceData("test", "192.0.0.0 - 192.255.255.255");
        addResourceData("one", "193.0.0.0 - 193.0.7.255");
        addResourceData("one", "2001:67c:370::/48");

        refreshResourceData();
    }

    // autnum

    @Test
    public void autnum_redirect() throws Exception {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/100"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/autnum/100"));
        }
    }

    @Test
    public void autnum_resource_not_found() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/101"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
        });
    }

    @Test
    public void autnum_empty_redirect_property() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/200"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
        });
    }

    @Test
    public void autnum_no_redirect_property() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/300"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
        });
    }

    // domain

    @Test
    public void redirect_domain_query() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "net/ripe/db/whois/rdap/domain/7.0.193.in-addr.arpa"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/domain/7.0.193.in-addr.arpa"));
        }
    }

    @Test
    public void redirect_ipv6_domain_query() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "net/ripe/db/whois/rdap/domain/0.7.3.0.c.7.6.0.1.0.0.2.ip6.arpa"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/domain/0.7.3.0.c.7.6.0.1.0.0.2.ip6.arpa"));
        }
    }

    @Test
    public void domain_outside_range() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "net/ripe/db/whois/rdap/domain/0.0.192.in-addr.arpa"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        });
    }

    // inetnum

    @Test
    public void inetnum_exact_match_redirect() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/193.0.0.0/21"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/ip/193.0.0.0/21"));
        }
    }

    @Test
    public void inetnum_child_redirect() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/193.0.0.1"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/ip/193.0.0.1"));
        }
    }

    @Test
    public void inetnum_outside_range() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/192.0.0.1"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        });
    }

    @Test
    public void inetnum_match_partial_delegation() {
        databaseHelper.addObject("" +
                "inetnum:      217.0.0.0 - 217.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "status:         OTHER\n" +
                "created:        2022-08-14T11:48:28Z\n" +
                "last-modified:  2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        databaseHelper.addObject("" +
                "inetnum:      217.180.128.0 - 217.180.191.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        addResourceData("test", "217.0.0.0 - 217.255.255.255");

        refreshResourceData();
        //217.180.192.0 - 217.180.255.255 is not allocated

        final Ip ip = RestTest.target(getPort(), String.format("rdap/%s", "ip/217.180.0.0/16"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), equalTo("217.0.0.0 - 217.255.255.255"));

    }

    @Test
    public void inetnum_redirect_partial_delegation() {
        databaseHelper.addObject("" +
                "inetnum:      217.180.128.0 - 217.180.191.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        addResourceData("one", "217.180.0.0 - 217.180.255.255");
        addResourceData("test", "217.180.128.0 - 217.180.191.255");

        refreshResourceData();
        //217.180.192.0 - 217.180.255.255 is not allocated
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/217.180.0.0/16"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one" +
                    ".net/ip/217.180.0.0/16"));
        }
    }

    @Test
    public void inetnum_larger_assigment_redirect_partial_delegation() {
        databaseHelper.addObject("" +
                "inetnum:      217.180.128.0 - 217.180.191.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        addResourceData("one", "217.0.0.0 - 217.255.255.255");
        addResourceData("test", "217.180.128.0 - 217.180.191.255");

        refreshResourceData();
        //217.180.192.0 - 217.180.255.255 is not allocated
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/217.180.0.0/16"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one" +
                    ".net/ip/217.180.0.0/16"));
        }
    }

    @Test
    public void inetnum_exact_match_redirect_illegal_character() {
        final String query = TelnetWhoisClient.queryLocalhost(getPort(), "GET /rdap/ip/193.0.0.0/21?redirect:%25{333*444} HTTP/1.1\nHost: localhost\nConnection: close\n");

        assertThat(query, containsString("Bad Request"));
        assertThat(query, containsString("Wrong URL format"));
    }

    // inet6num

    @Test
    public void inet6num_exact_match_redirect() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/2001:67c:370::/48"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/ip/2001:67c:370::/48"));
        }
    }

    @Test
    public void inet6num_child_redirect() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/2001:67c:370::1234"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/ip/2001:67c:370::1234"));
        }
    }

    @Test
    public void inet6num_outside_range() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/2002::/32"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        });
    }

    // helper methods

    private void deleteResourceData(final String source, final String resource) {
        databaseHelper.getInternalsTemplate().update("DELETE FROM authoritative_resource WHERE source = ? AND resource = ?", source, resource);
    }

    private void addResourceData(final String source, final String resource) {
        databaseHelper.getInternalsTemplate().update("INSERT INTO authoritative_resource (source, resource) VALUES (?, ?)", source, resource);
    }

    private void refreshResourceData() {
        authoritativeResourceData.refreshGrsSources();
    }

}
