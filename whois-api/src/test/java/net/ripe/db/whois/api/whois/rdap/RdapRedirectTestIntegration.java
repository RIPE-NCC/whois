package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import org.junit.AfterClass;
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

    @Test
    public void redirect_to_url() throws Exception {
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
    public void resource_not_found() {
        RestTest.target(getPort(), String.format("rdap/%s", "autnum/101"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void empty_redirect_property() {
        addResourceData("two", "AS200");
        refreshResourceData();

        RestTest.target(getPort(), String.format("rdap/%s", "autnum/200"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void no_redirect_property() {
        addResourceData("three", "AS300");
        refreshResourceData();

        RestTest.target(getPort(), String.format("rdap/%s", "autnum/300"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    private void addResourceData(final String source, final String resource) {
        databaseHelper.getInternalsTemplate().update("INSERT INTO authoritative_resource VALUES (?, ?)", source, resource);
    }

    private void refreshResourceData() {
        databaseHelper.getInternalsTemplate().update(
                "DELETE FROM scheduler WHERE task = ?",
                AuthoritativeResourceImportTask.class.getSimpleName());

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO scheduler (host, done, date, task) VALUES ('localhost', ?, ?, ?)",
                dateTimeProvider.getCurrentDateTime().getMillisOfDay() + 1,
                dateTimeProvider.getCurrentDate().toString(),
                AuthoritativeResourceImportTask.class.getSimpleName());

        authoritativeResourceData.refreshAuthoritativeResourceCache();
    }
}
