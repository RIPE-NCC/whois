package net.ripe.db.whois.query.integration;


import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AbuseCTestIntegration extends AbstractQueryIntegrationTest {

    private static final String[] BASE_OBJECTS = {
            "mntner:        TEST-MNT\n" +
            "nic-hdl:       ABU-TEST\n" +
            "source:        TEST",

            "role:          Abuse Role\n" +
            "address:       APNIC, see http://www.apnic.net\n" +
            "e-mail:        bitbucket@ripe.net\n" +
            "admin-c:       ABU-TEST\n" +
            "tech-c:        ABU-TEST\n" +
            "nic-hdl:       ABU-TEST\n" +
            "abuse-mailbox: abuse@ripe.net\n" +
            "mnt-by:        TEST-MNT\n" +
            "changed:       ripe-dbm@ripe.net 20010411\n" +
            "source:        TEST",

            "organisation:  ORG-TEST-1\n" +
            "abuse-c:       ABU-TEST\n" +
            "source:        TEST",

            "inetnum:       173.0.0.0 - 173.255.255.255\n" +
            "org:           ORG-TEST-1\n" +
            "netname:       NN\n" +
            "status:        OTHER\n" +
            "source:        TEST",

            "mntner:        RIPE-NCC-HM-MNT\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "source:        TEST",

            "inetnum:       18.0.0.0 - 18.255.255.255\n" +
            "netname:       NN\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "source:        TEST",

            "inetnum:       0.0.0.0 - 255.255.255.255\n" +
            "netname:       NN\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "source:        TEST",

            "aut-num:       AS102\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "source:        TEST",

            "aut-num:       AS103\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "org:           ORG-TEST-1\n" +
            "source:        TEST",

            "role:          A Role\n" +
            "nic-hdl:       NIC2-TEST\n" +
            "abuse-mailbox: shown@abuse.net\n" +
            "source:        TEST",

            "role:          B Role\n" +
            "nic-hdl:       NIC3-TEST\n" +
            "abuse-mailbox: notshown@abuse.net\n" +
            "source:        TEST",

            "organisation:  ORG-TEST-2\n" +
            "abuse-c:       NIC2-TEST\n" +
            "source:        TEST",

            "inetnum:       193.0.0.0 - 193.255.255.255\n" +
            "netname:       RIPE\n" +
            "org:           ORG-TEST-2\n" +
            "admin-c:       NIC2-TEST\n" +
            "tech-c:        NIC3-TEST\n" +
            "source:        TEST",

            "inetnum:       193.0.0.0 - 193.0.0.255\n" +
            "netname:       RIPE\n" +
            "admin-c:       NIC2-TEST\n" +
            "tech-c:        NIC3-TEST\n" +
            "source:        TEST",

            "route:         193.0.0.0/8\n" +
            "descr:         RIPE-NCC\n" +
            "origin:        A201\n" +
            "source:        TEST"
    };

    @Autowired
    private IpTreeUpdater ipTreeUpdater;

    @Before
    public void setup() throws Exception {
        for (String next : BASE_OBJECTS) {
            databaseHelper.addObject(RpslObject.parse(next));
        }
        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @After
    public void shutdown() {
        queryServer.stop(true);
    }

    @Test
    public void forwardLookupInetnum() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "173.0.0.0");

        assertThat(response, containsString("% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'"));
    }

    @Test
    public void simpleLookupInetnum() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rBGxTinetnum 173.0.0.0/8");

        assertThat(response, containsString("" +
                "% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'\n" +
                "\n" +
                "inetnum:        173.0.0.0 - 173.255.255.255"));
    }

    @Test
    public void simpleLookupChildInetnum() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "193.0.0.0");

        assertThat(response, containsString("" +
                "% Abuse contact for '193.0.0.0 - 193.0.0.255' is 'shown@abuse.net'\n" +
                "\n" +
                "inetnum:        193.0.0.0 - 193.0.0.255"));
    }

    @Test
    public void dashBGivesAbuseCMessage_hasContact() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-b 173.0.0.5");

        assertThat(response, not(containsString("% Abuse contact for '173.0.0.0 - 173.255.255.255' is 'abuse@ripe.net'")));
        assertThat(response, containsString("" +
                "inetnum:        173.0.0.0 - 173.255.255.255\n" +
                "abuse-mailbox:  abuse@ripe.net"));
    }

    @Test
    public void dashBGivesAbuseCMessage_hasNoContact() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-b 18.0.0.0");

        assertThat(response, containsString("inetnum:        18.0.0.0 - 18.255.255.255"));

        assertThat(response, not(containsString("" +
                "inetnum:        173.0.0.0 - 173.255.255.255\n" +
                "abuse-mailbox:  abuse@ripe.net")));
    }

    @Test
    public void rootObjectShowsNoMessage() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "0.0.0.5");
        assertThat(response, not(containsString("Abuse")));
    }

    @Test
    public void autnum_hasNoContacts() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "AS102");
        assertThat(response, containsString(
                "% Information related to 'AS102'\n" +
                "\n" +
                "% No abuse contact registered for AS102\n" +
                "\n" +
                "aut-num:        AS102"));
        assertThat(response, not(containsString("Abuse contact")));
    }

    @Test
    public void autnum_hasContacts() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "AS103");
        assertThat(response, containsString("Abuse contact for 'AS103' is 'abuse@ripe.net'"));
    }

    @Test
    public void brief_query_shows_abusemailbox_twice() {
        final String briefResponse = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-b 193.0.0.0");
        assertThat(briefResponse, not(containsString("notshown@abuse.net")));
    }
}
