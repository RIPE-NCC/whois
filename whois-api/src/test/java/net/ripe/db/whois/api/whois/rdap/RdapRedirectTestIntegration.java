package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
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
import java.sql.SQLException;
import java.util.List;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Category(IntegrationTest.class)
public class RdapRedirectTestIntegration extends AbstractIntegrationTest {

    @Autowired protected List<ApplicationService> applicationServices;
    @Autowired AuthoritativeResourceData authoritativeResourceData;

    @Before
    public void setup() throws Exception {
        loadScripts(databaseHelper.getInternalsTemplate(), "rdap-test-data.sql");
        databaseHelper.getInternalsTemplate().update("INSERT INTO scheduler (host, done, date, task) VALUES('localhost', ?, ?, ?)", dateTimeProvider.getCurrentDateTime().getMillisOfDay(), new LocalDate().toString(), "AuthoritativeResourceImportTask");
        DatabaseHelper.dumpSchema(databaseHelper.getInternalsTemplate().getDataSource());

        dateTimeProvider.setTime(new LocalDateTime().plusHours(1));
        authoritativeResourceData.refreshAuthoritativeResourceCache();
    }

    @AfterClass
    public static void clearProperties() throws IOException {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.apnic");
        System.clearProperty("rdap.redirect.afrinic");
        System.clearProperty("rdap.redirect.arin");
        System.clearProperty("rdap.redirect.lacnic");
        System.clearProperty("grs.import.arin.resourceDataUrl");
    }

    @BeforeClass
    public static void setProperties() throws IOException {
        System.setProperty("rdap.sources", "apnic,afrinic,arin,lacnic");
        System.setProperty("rdap.redirect.apnic", "http://rdap.apnic.net");
        System.setProperty("rdap.redirect.afrinic", "");
        System.setProperty("rdap.redirect.arin", "http://rdappilot.arin.net");
        System.setProperty("rdap.redirect.lacnic", "http://restfulwhoisv2.labs.lacnic.net");
        System.setProperty("grs.import.arin.resourceDataUrl", "");

        DatabaseHelper.addGrsDatabases("apnic-GRS", "afrinic-GRS", "arin-GRS", "lacnic-GRS");
    }

    @Test
    //TODO behaves correctly up til whoisrdapservice.redirect.getUri - what happens with the response is anyone's guess
    public void foundForRedirect() throws SQLException {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/173")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("http://rdap.apnic.net/autnum/173"));
        }
    }

    @Test(expected = NotFoundException.class)
    public void notFoundForRedirect() throws SQLException {
        RestTest.target(getPort(), String.format("rdap/%s", "autnum/200")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    @Test
    //TODO should we return 404 or redirect if url is not present?
    public void foundButNoRedirectUrl() {
        try {
            RestTest.target(getPort(), String.format("rdap/%s", "autnum/800")).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is(""));
        }
    }
}
