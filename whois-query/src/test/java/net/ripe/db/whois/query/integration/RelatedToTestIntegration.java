package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class RelatedToTestIntegration extends AbstractQueryIntegrationTest {

    @BeforeEach
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObject(RpslObject.parse("mntner:RIPE-NCC-MNT\nnic-hdl:AP111-RIPE"));
        databaseHelper.addObject(RpslObject.parse("" +
                "role:         Asia Pacific Network Information Centre\n" +
                "address:      APNIC, see http://www.apnic.net\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "admin-c:      APNC1-RIPE\n" +
                "tech-c:       APNC1-RIPE\n" +
                "nic-hdl:      APNC1-RIPE\n" +
                "remarks:      For more information on APNIC assigned blocks, connect\n" +
                "remarks:      to APNIC's whois database, whois.apnic.net.\n" +
                "mnt-by:       RIPE-NCC-MNT\n" +
                "source:       RIPE"));

        queryServer.start();
    }

    @AfterEach
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void references_self_related_to_grouping() {
        references_self("APNC1-RIPE");
    }

    @Test
    public void references_self_related_to_non_grouping() {
        references_self("-G APNC1-RIPE");
    }

    private void references_self(final String query) {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);

        final String check = "role:           Asia Pacific Network Information Centre\n";
        assertThat(response, containsString(check));
        assertThat(response.indexOf(check), is(response.lastIndexOf(check)));   // Object should appear only once
    }
}
