package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class RelatedToTestIntegration extends AbstractQueryIntegrationTest {

    @Before
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
                "changed:      ripe-dbm@ripe.net 20010411\n" +
                "source:       RIPE"));

        queryServer.start();
    }

    @After
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
        final String response = DummyWhoisClient.query(QueryServer.port, query);

        final String check = "role:           Asia Pacific Network Information Centre\n";
        assertThat(response, containsString(check));
        assertTrue("Object should appear only once", response.indexOf(check) == response.lastIndexOf(check));
    }
}
