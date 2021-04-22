package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Category(IntegrationTest.class)
public class NrtmQueryTestIntegration extends AbstractNrtmIntegrationBase {

    @Before
    public void before() throws InterruptedException {
        nrtmServer.start();
    }

    @After
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void versionQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-q version");

        assertThat(response, containsString("% nrtm-server"));
    }

    @Test
    public void sourcesQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-q sources");

        assertThat(response, containsString("TEST:3:X:0-0"));
        assertThat(response, containsString("TEST-NONAUTH:3:X:0-0"));
    }

    @Test
    public void emptyQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "\n");

        assertThat(response, containsString("no flags passed"));
    }


    @Test
    public void nrtm_keeps_timestamp_attributes() {
        databaseHelper.addObject("" +
                "role:          Denis Walker\n" +
                "nic-hdl:       DW-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        test@ripe.net\n" +
                "created:       2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "source:        TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "role:           Denis Walker\n" +
                "nic-hdl:        DW-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST"));
        assertThat(response, containsString("remarks:        * THIS OBJECT IS MODIFIED"));
    }

    @Test
    public void should_not_have_blank_lines_between_sources() {
        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-q sources");

        assertThat(response, containsString(
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                        "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                        "\n" +
                        "TEST:3:X:0-0\n" +
                        "TEST-NONAUTH:3:X:0-0\n" +
                        "\n"
        ));
    }

}
