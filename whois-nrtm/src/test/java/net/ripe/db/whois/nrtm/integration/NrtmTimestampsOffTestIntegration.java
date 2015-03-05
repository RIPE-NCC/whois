package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class NrtmTimestampsOffTestIntegration extends AbstractNrtmIntegrationBase {

    @Before
    public void before() throws Exception {
        setTimestampsModeOff(false);
        nrtmServer.start();
    }

    @After
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void nrtm_optional_timestamp_attributes_mode_off() {
        setTimestampsModeOff(true);

        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        noreply@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String legacyResponse = TelnetWhoisClient.queryLocalhost(NrtmServer.getLegacyPort(), "-g TEST:3:1-LAST");

        assertThat(legacyResponse, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "source:         TEST\n"));

        assertThat(legacyResponse, not(containsString("created")));
        assertThat(legacyResponse, not(containsString("last-modified")));

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        ***@ripe.net 20000101\n" +
                "source:         TEST\n"));

        assertThat(response, not(containsString("created")));
        assertThat(response, not(containsString("last-modified")));
    }

    @Test
    public void nrtm_retains_optional_timestamp_attributes_mode_on() {
        setTimestampsModeOff(false);

        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        noreply@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String legacyResponse = TelnetWhoisClient.queryLocalhost(NrtmServer.getLegacyPort(), "-g TEST:3:1-LAST");

        assertThat(legacyResponse, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "changed:        ***@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));
    }

    @Test
    public void nrtm_discards_optional_timestamp_attributes_mode_off_organisation() {
        setTimestampsModeOff(true);

        databaseHelper.addObject("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        noreply@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String legacyResponse = TelnetWhoisClient.queryLocalhost(NrtmServer.getLegacyPort(), "-g TEST:3:1-LAST");

        assertThat(legacyResponse, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "source:         TEST\n"));

        assertThat(legacyResponse, not(containsString("created")));
        assertThat(legacyResponse, not(containsString("last-modified")));


        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        ***@ripe.net 20000101\n" +
                "source:         TEST\n"));

        assertThat(response, not(containsString("created")));
        assertThat(response, not(containsString("last-modified")));
    }

    @Test
    public void nrtm_optional_timestamp_attributes_mode_on_organisation() {
        setTimestampsModeOff(false);

        databaseHelper.addObject("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String legacyResponse = TelnetWhoisClient.queryLocalhost(NrtmServer.getLegacyPort(), "-g TEST:3:1-LAST");

        assertThat(legacyResponse, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        unread@ripe.net 20000101\n" +
                "source:         TEST\n"));

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "org-type:       OTHER\n" +
                "changed:        ***@ripe.net 20000101\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n"));
    }

    private void setTimestampsModeOff(boolean timestampsOff){
        System.setProperty("rpsl.timestamps.off", String.valueOf(timestampsOff));
    }
}
