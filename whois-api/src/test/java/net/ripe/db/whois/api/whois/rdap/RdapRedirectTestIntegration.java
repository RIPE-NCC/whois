package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class RdapRedirectTestIntegration extends AbstractIntegrationTest {

    @Autowired AuthoritativeResourceData authoritativeResourceData;

    @BeforeClass
    public static void setProperties() throws IOException {
        System.setProperty("rdap.sources", "ONE-GRS,TWO-GRS,THREE-GRS");
        System.setProperty("rdap.redirect.one", "https://rdap.one.net");
        System.setProperty("rdap.redirect.two", "");
        // no property set for three-grs
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
        DatabaseHelper.addGrsDatabases("ONE-GRS", "TWO-GRS", "THREE-GRS");
    }

    @AfterClass
    public static void clearProperties() throws IOException {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.one");
        System.clearProperty("rdap.redirect.two");
        System.clearProperty("rdap.public.baseUrl");
    }

    @Before
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
    }

    // autnum

    @Test
    public void autnum_redirect() throws Exception {
        addResourceData("one", "AS100");
        refreshResourceData();

        try {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/100"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.one.net/autnum/100"));
        }
    }

    @Test(expected = NotFoundException.class)
    public void autnum_resource_not_found() {
        RestTest.target(getPort(), String.format("rdap/%s", "autnum/101"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void autnum_empty_redirect_property() {
        addResourceData("two", "AS200");
        refreshResourceData();

        RestTest.target(getPort(), String.format("rdap/%s", "autnum/200"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void autnum_no_redirect_property() {
        addResourceData("three", "AS300");
        refreshResourceData();

        RestTest.target(getPort(), String.format("rdap/%s", "autnum/300"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    // inetnum

    @Test
    public void inetnum_exact_match_redirect() {
        addResourceData("one", "193.0.0.0 - 193.0.7.255");
        refreshResourceData();

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
        addResourceData("one", "193.0.0.0 - 193.0.7.255");
        refreshResourceData();

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
        addResourceData("one", "193.0.0.0 - 193.0.7.255");
        refreshResourceData();

        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/192.0.0.1"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final NotFoundException e) {
            // expected
        }
    }

    // inet6num

    @Test
    public void inet6num_exact_match_redirect() {
        addResourceData("one", "2001:67c:370::/48");
        refreshResourceData();

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
        addResourceData("one", "2001:67c:370::/48");
        refreshResourceData();

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
        addResourceData("one", "2001:67c:370::/48");
        refreshResourceData();

        try {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/2002::/32"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
            fail();
        } catch (final NotFoundException e) {
            // expected
        }
    }

    // helper methods

    private void addResourceData(final String source, final String resource) {
        databaseHelper.getInternalsTemplate().update("INSERT INTO authoritative_resource (source, resource) VALUES (?, ?)", source, resource);
    }

    private void refreshResourceData() {
        databaseHelper.getInternalsTemplate().update(
                "DELETE FROM scheduler WHERE task = ?",
                AuthoritativeResourceImportTask.class.getSimpleName());

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO scheduler (host, done, date, task) VALUES ('localhost', ?, ?, ?)",
                testDateTimeProvider.getCurrentDateTime().getMillisOfDay() + 1,
                testDateTimeProvider.getCurrentDate().toString(),
                AuthoritativeResourceImportTask.class.getSimpleName());

        authoritativeResourceData.refreshAuthoritativeResourceCache();
    }
}
