package net.ripe.db.whois.query.integration;


import com.google.common.collect.Lists;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AbuseCTestIntegration extends AbstractWhoisIntegrationTest {
    @Autowired
    private IpTreeUpdater ipTreeUpdater;

    @Before
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObject(RpslObject.parse("mntner:TEST-MNT\nnic-hdl:ABU-TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "role:         Abuse Role\n" +
                "address:      APNIC, see http://www.apnic.net\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "admin-c:      ABU-TEST\n" +
                "tech-c:       ABU-TEST\n" +
                "nic-hdl:      ABU-TEST\n" +
                "abuse-mailbox:      abuse@ripe.net\n" +
                "mnt-by:       TEST-MNT\n" +
                "changed:      ripe-dbm@ripe.net 20010411\n" +
                "source:       TEST"));
        databaseHelper.addObject(RpslObject.parse("organisation: ORG-TEST-1\nabuse-c: ABU-TEST"));
        databaseHelper.addObject(RpslObject.parse("inetnum:173.0.0.0 - 173.255.255.255\norg: ORG-TEST-1\nnetname: NN\nstatus:OTHER"));

        final RpslObject inetnum = RpslObject.parse("inetnum:18.0.0.0 - 18.255.255.255\nnetname: NN\nmnt-by: RIPE-NCC-HM-MNT");
        final RpslObject root = RpslObject.parse("inetnum:0.0.0.0 - 255.255.255.255\nnetname: NN\nmnt-by: RIPE-NCC-HM-MNT");
        final RpslObject rsMaintainer = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT");
        databaseHelper.addObjects(Lists.newArrayList(inetnum, rsMaintainer, root));
        databaseHelper.addObject("aut-num: AS102\nmnt-by: RIPE-NCC-HM-MNT");
        databaseHelper.addObject("aut-num: AS103\nmnt-by: RIPE-NCC-HM-MNT\norg: ORG-TEST-1");

        databaseHelper.addObject("role: A Role\nnic-hdl: NIC2-TEST\nabuse-mailbox: shown@abuse.net");
        databaseHelper.addObject("role: B Role\nnic-hdl: NIC3-TEST\nabuse-mailbox: notshown@abuse.net");
        databaseHelper.addObject("organisation: ORG-TEST-2\nabuse-c: NIC2-TEST");
        databaseHelper.addObject("inetnum: 193.0.0.0 - 193.255.255.255\nnetname: RIPE\norg: ORG-TEST-2\nadmin-c: NIC2-TEST\ntech-c: NIC3-TEST");
        databaseHelper.addObject("route: 193.0.0.0/8\ndescr: RIPE-NCC\norigin: A201");

        ipTreeUpdater.rebuild();

        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void forwardLookupInetnum() {
        final String response = DummyWhoisClient.query(QueryServer.port, "173.0.0.0");

        assertThat(response, containsString("% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'"));
    }

    @Test
    public void simpleLookupInetnum() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-rBGxTinetnum 173.0.0.0/8");

        assertThat(response, containsString("" +
                "% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'\n\n" +
                "inetnum:        173.0.0.0 - 173.255.255.255"));
    }

    @Test
    public void dashBGivesAbuseCMessage_hasContact() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-b 173.0.0.5");

        assertThat(response, not(containsString("% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'")));
        assertThat(response, containsString("" +
                "inetnum:        173.0.0.0 - 173.255.255.255\n" +
                "abuse-mailbox:  abuse@ripe.net"));
    }

    @Test
    public void dashBGivesAbuseCMessage_hasNoContact() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-b 18.0.0.0");

        assertThat(response, containsString("" +
                "inetnum:        18.0.0.0 - 18.255.255.255"));

        assertThat(response, not(containsString("" +
                "inetnum:        173.0.0.0 - 173.255.255.255\n" +
                "abuse-mailbox:  abuse@ripe.net")));
    }

    @Test
    public void rootObjectShowsNoMessage() {
        final String response = DummyWhoisClient.query(QueryServer.port, "0.0.0.5");
        assertThat(response, not(containsString("Abuse")));
    }

    @Test
    public void autnum_hasNoContacts() {
        final String response = DummyWhoisClient.query(QueryServer.port, "AS102");
        assertThat(response, containsString(
                "% Information related to 'AS102'\n" +
                        "\n" +
                        "aut-num:        AS102"));
        assertThat(response, not(containsString("Abuse contact")));
    }

    @Test
    public void autnum_hasContacts() {
        final String response = DummyWhoisClient.query(QueryServer.port, "AS103");
        assertThat(response, containsString("Abuse contact for 'AS103' is 'abuse@ripe.net'"));
    }

    @Test
    public void brief_query_shows_abusemailbox_twice() {
        final String briefResponse = DummyWhoisClient.query(QueryServer.port, "-b 193.0.0.0");
        assertThat(briefResponse, not(containsString("notshown@abuse.net")));
    }
}
