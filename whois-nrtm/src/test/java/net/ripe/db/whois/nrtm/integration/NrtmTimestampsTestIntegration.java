package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class NrtmTimestampsTestIntegration extends AbstractNrtmIntegrationBase {

    @BeforeEach
    public void before() {
        nrtmServer.start();
    }

    @AfterEach
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void nrtm_retains_timestamp_attributes() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
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
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));
    }
}
