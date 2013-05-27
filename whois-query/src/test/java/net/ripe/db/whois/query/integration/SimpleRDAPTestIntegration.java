package net.ripe.db.whois.query.integration;

/**
 * Created with IntelliJ IDEA.
 * User: andrew-old
 * Date: 24/05/13
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

@Category(IntegrationTest.class)
public class SimpleRDAPTestIntegration extends AbstractWhoisIntegrationTest {
    private static final String END_OF_HEADER = "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n\n";

    @Autowired
    IpTreeUpdater ipTreeUpdater;
    @Autowired
    DateTimeProvider dateTimeProvider;

    @Before
    public void startupWhoisServer() {
        final RpslObject person = RpslObject.parse("person: ADM-TEST\naddress: address\nphone: +312342343\nmnt-by:RIPE-NCC-HM-MNT\nadmin-c: ADM-TEST\nchanged: dbtest@ripe.net 20120707\nnic-hdl: ADM-TEST");
        final RpslObject mntner = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\ndescr: description\nadmin-c: ADM-TEST");
        databaseHelper.addObjects(Lists.newArrayList(person, mntner));

        databaseHelper.addObject("inetnum: 81.0.0.0 - 82.255.255.255\nnetname: NE\nmnt-by:RIPE-NCC-HM-MNT");
        databaseHelper.addObject("domain: 117.80.81.in-addr.arpa");
        databaseHelper.addObject("inetnum: 81.80.117.237 - 81.80.117.237\nnetname: NN\nstatus: OTHER");
        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop();
    }

    @Test
    public void testLoggingNonProxy() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-rBGxTinetnum 81.80.117.237 - 81.80.117.237");
        //assertThat(response, containsString("81.80.117.237 - 81.80.117.237"));
    }
}
