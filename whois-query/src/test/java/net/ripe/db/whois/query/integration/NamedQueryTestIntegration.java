package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class NamedQueryTestIntegration extends AbstractWhoisIntegrationTest {

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
        queryServer.stop();
    }

    @Test
    public void organisationQueryCaseInsensitive() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-rT oa orG-Zv1-RipE");

        assertThat(response, containsString("organisation:   ORG-ZV1-RIPE"));
    }

    @Test
    public void personQueryCaseInsensitive() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-rT person dENIs WalKeR");

        assertThat(response, containsString("person:         Denis Walker"));
    }

    @Test
    public void roleQueryCaseInsensitive() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-rT role AltERNa InterTRAde ripe TEaM");

        assertThat(response, containsString("role:           Alterna Intertrade RIPE team"));
    }

    @Test
    public void findPersonByNicHdlNotFiltered() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-r -B -T person DH3037-RIPE");

        assertThat(response, containsString("Information related to 'DH3037-RIPE'"));
        assertThat(response, not(containsString("filtered")));
    }

    @Test
    public void findPersonByNicHdlIsFiltered() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-r -T person DH3037-RIPE");

        assertThat(response, containsString("Information related to 'DH3037-RIPE'"));
        assertThat(response, containsString("filtered"));
    }

    @Test
    public void findPersonNameMatchesNicHdl() throws Exception {
        String response = DummyWhoisClient.query(QueryServer.port, "-r -B -T person ASAK");

        assertThat(response, containsString("person:         ASAK"));
        assertThat(response, containsString("nic-hdl:        ASAK"));
        assertThat(response.indexOf("person:         ASAK"), is(response.lastIndexOf("person:         ASAK")));
        assertThat(response, not(containsString("# Filtered")));
    }
}
