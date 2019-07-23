package net.ripe.db.whois.query.integration;

import com.google.common.base.Strings;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class NamedQueryTestIntegration extends AbstractQueryIntegrationTest {

    @Before
    public void startupWhoisServer() {
        databaseHelper.addObject("organisation:ORG-ZV1-RIPE");
        databaseHelper.addObject("person:Denis Walker\nnotify:someone@ripe.net\ne-mail:someone@ripe.net\nnic-hdl:DH3037-RIPE");
        databaseHelper.addObject("person:ASAK\nnic-hdl:ASAK");
        databaseHelper.addObject("role:Alterna Intertrade RIPE team\nnic-hdl:AIRT");
        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void organisationQueryCaseInsensitive() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT oa orG-Zv1-RipE");

        assertThat(response, containsString("organisation:   ORG-ZV1-RIPE"));
    }

    @Test
    public void personQueryCaseInsensitive() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT person dENIs WalKeR");

        assertThat(response, containsString("person:         Denis Walker"));
    }

    @Test
    public void roleQueryCaseInsensitive() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT role AltERNa InterTRAde ripe TEaM");

        assertThat(response, containsString("role:           Alterna Intertrade RIPE team"));
    }

    @Test
    public void findPersonByNicHdlNotFiltered() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -B -T person DH3037-RIPE");

        assertThat(response, containsString("Information related to 'DH3037-RIPE'"));
        assertThat(response, not(containsString("filtered")));
    }

    @Test
    public void findPersonByNicHdlIsFiltered() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -T person DH3037-RIPE");

        assertThat(response, containsString("Information related to 'DH3037-RIPE'"));
        assertThat(response, containsString("filtered"));
    }

    @Test
    public void findPersonNameMatchesNicHdl() throws Exception {
        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -B -T person ASAK");

        assertThat(response, containsString("person:         ASAK"));
        assertThat(response, containsString("nic-hdl:        ASAK"));
        assertThat(response.indexOf("person:         ASAK"), is(response.lastIndexOf("person:         ASAK")));
        assertThat(response, not(containsString("# Filtered")));
    }

    @Test
    public void tooManyArguments() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT person " + Strings.repeat("arg ", 62));

        assertThat(response, containsString("" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "%ERROR:118: too many arguments supplied\n" +
                "%\n" +
                "% Too many arguments supplied.\n" +
                "\n" +
                "% This query was served by"));
    }

    @Test
    public void almostTooManyArguments() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT person " + Strings.repeat("arg ", 60));

        assertThat(response, containsString("%ERROR:101: no entries found"));
    }

    @Ignore("TODO: [ES] exception thrown as query string not normalised")
    @Test
    public void queryStringNotNormalised() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "\u200E2019 11:35] ok");

        assertThat(response, containsString("test"));
    }
}
