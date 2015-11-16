package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class NrtmTimestampsTestIntegration extends AbstractNrtmIntegrationBase {

    @Before
    public void before() throws Exception {
        nrtmServer.start();
    }

    @After
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void nrtm_retains_timestamp_attributes() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        noreply@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));
    }

    @Test
    public void nrtm_timestamp_attributes_organisation() {
        databaseHelper.addObject("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));
    }
}
