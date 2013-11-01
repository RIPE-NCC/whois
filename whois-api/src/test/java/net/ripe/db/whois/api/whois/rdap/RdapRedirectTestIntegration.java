package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

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
        System.setProperty("rdap.sources", "RANDOM-GRS");
        System.setProperty("rdap.redirect.random", "https://rdap.random.net");
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
        DatabaseHelper.addGrsDatabases("RANDOM-GRS");
    }

    @AfterClass
    public static void clearProperties() throws IOException {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.random");
        System.clearProperty("rdap.public.baseUrl");
    }

    @Test
    public void lookup_autnum_redirect_to_test() throws Exception {
        addResourceData("random", "AS173");
        refreshResourceData();

        try {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/173"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.random.net/rdap/autnum/173"));
        }
    }

//    @Test
//    //TODO behaves correctly up til whoisrdapservice.redirect.getUri - what happens with the response is anyone's guess
//    public void foundForRedirect() throws SQLException {
//        try {
//            RestTest.target(getPort(), String.format("rdap/%s", "autnum/173")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
//        } catch (final RedirectionException e) {
//            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("http://rdap.apnic.net/autnum/173"));
//        }
//    }
//
//    @Test(expected = NotFoundException.class)
//    public void notFoundForRedirect() throws SQLException {
//        RestTest.target(getPort(), String.format("rdap/%s", "autnum/200")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
//    }
//
//    @Test
//    //TODO should we return 404 or redirect if url is not present?
//    public void foundButNoRedirectUrl() {
//        try {
//            RestTest.target(getPort(), String.format("rdap/%s", "autnum/800")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
//        } catch (final RedirectionException e) {
//            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is(""));
//        }
//    }


    private void addResourceData(final String source, final String resource) {
        databaseHelper.getInternalsTemplate().update("INSERT INTO authoritative_resource VALUES (?, ?)", source, resource);
    }

    private void refreshResourceData() {
        databaseHelper.getInternalsTemplate().update(
                "DELETE FROM scheduler WHERE task = ?",
                AuthoritativeResourceImportTask.class.getName());

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO scheduler (host, done, date, task) VALUES ('localhost', ?, ?, ?)",
                dateTimeProvider.getCurrentDateTime().getMillisOfDay() + 1,
                dateTimeProvider.getCurrentDate().toString(),
                AuthoritativeResourceImportTask.class.getSimpleName());

        authoritativeResourceData.refreshAuthoritativeResourceCache();
    }
}
