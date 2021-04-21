package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Category(IntegrationTest.class)
public class SimpleTestIntegration extends AbstractNrtmIntegrationBase {

    @Value("${nrtm.update.interval:15}") private String updateIntervalString;

    private int updateInterval;

    @Before
    public void before() throws InterruptedException {
        updateInterval = Integer.valueOf(updateIntervalString);
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
    public void queryKeepaliveNoPreExistingObjects() {
        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-2 -k", (updateInterval + 1) * 1000);

        assertThat(response, containsString("%ERROR:401: invalid range"));
    }

    @Test
    public void queryKeepAliveNoPreExistingObjectsOneNewObject() {
        databaseHelper.addObject(RpslObject.parse("mntner:test\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(NrtmServer.getPort(), "-g TEST:3:1-1 -k", (updateInterval + 1));

        client.start();
        databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST"));
        String response = client.end();

        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveNoPreExistingObjectsOneNewObject2() {
        databaseHelper.addObject(RpslObject.parse("mntner:test\nsource:TEST-NONAUTH"));
        AsyncNrtmClient client = new AsyncNrtmClient(NrtmServer.getPort(), "-g TEST-NONAUTH:3:1-1 -k", (updateInterval + 1));

        client.start();
        databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST-NONAUTH"));
        String response = client.end();

        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveOnePreExistingObjectsOneNewObject() {
        databaseHelper.addObject(RpslObject.parse("mntner:testmntner\nmnt-by:testmntner\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(NrtmServer.getPort(), "-g TEST:3:1-LAST -k", (updateInterval + 1));

        client.start();
        super.databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST"));
        String response = client.end();

        assertThat(response, containsString("mntner:         testmntner"));
        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveOnePreExistingObjectDifferentSource() {
        databaseHelper.addObject(RpslObject.parse("mntner:testmntner\nmnt-by:testmntner\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(NrtmServer.getPort(), "-g TEST-NONAUTH:3:1-LAST -k", (updateInterval + 1));

        client.start();
        //super.databaseHelper.addObject(RpslObject.parse("aut-num:AS4294967207\nsource:TEST-NONAUTH"));
        String response = client.end();

        assertThat(response, not(containsString("testmntner")));
        //assertThat(response, not(containsString("AS4294967207")));
    }

    @Test
    public void mirrorQueryOneSerialEntry() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-1");

        assertThat(response, containsString("aut-num:        AS4294967207"));
    }

    @Test
    public void mirrorQueryOneSerialEntryNonAuth() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST-NONAUTH");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST-NONAUTH:3:1-1");

        assertThat(response, containsString("aut-num:        AS4294967207"));
    }

    @Test
    public void mirrorQueryMultipleSerialEntry() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-3");

        assertThat(response, containsString("ADD 1"));
        assertThat(response, containsString("AS4294967207"));
        assertThat(response, not(containsString("ADD 2")));
        assertThat(response, not(containsString("DW-RIPE")));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("DEV-MNT"));
    }

    @Test
    public void mirrorQueryMultipleSerialEntryMixedSourceTypeAuth() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("aut-num:AS5294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS6294967207\nsource:TEST");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE\nsource:TEST");
        databaseHelper.addObject("mntner:DEV-MNT\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-3");

        assertThat(response, containsString("ADD 1"));
        assertThat(response, containsString("AS4294967207"));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("AS6294967207"));
        assertThat(response, containsString("%END TEST"));
        assertThat(response, not(containsString("DW-RIPE")));
        assertThat(response, not(containsString("TEST-NONAUTH")));
    }

    @Test
    public void mirrorQueryMultipleSerialEntryMixedSourceTypeNonAuth() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("aut-num:AS5294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS6294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST-NONAUTH:3:1-3");

        assertThat(response, containsString("ADD 2"));
        assertThat(response, containsString("AS5294967207"));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("AS6294967207"));
        assertThat(response, containsString("%END TEST-NONAUTH"));
        assertThat(response, not(containsString("DW-RIPE")));
    }


    @Test
    public void queryKeepAliveMultipleSerialEntryMixedSource() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS5294967207\nsource:TEST");
        databaseHelper.addObject("aut-num:AS6294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS7294967207\nsource:TEST-NONAUTH");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST-NONAUTH:3:1-LAST");

        assertThat(response, containsString("ADD 1"));
        assertThat(response, containsString("AS4294967207"));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("AS6294967207"));
        assertThat(response, containsString("ADD 4"));
        assertThat(response, containsString("AS7294967207"));
        assertThat(response, containsString("source:         TEST-NONAUTH"));
        assertThat(response, containsString("%END TEST-NONAUTH"));
    }

    @Test
    public void mirrorQueryOutofRange() {
        databaseHelper.addObject("aut-num:AS4294967207");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:2-4");

        assertThat(response, containsString("invalid range: Not within 1-1"));
    }

    @Test
    public void mirrorQueryWithLastKeyword() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("ADD 1"));
        assertThat(response, containsString("AS4294967207"));
        assertThat(response, not(containsString("ADD 2")));
        assertThat(response, not(containsString("DW-RIPE")));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("DEV-MNT"));
    }

    @Test
    public void mirror_query_abuse_mailbox() {
        databaseHelper.addObject("" +
                "role:          Denis Walker\n" +
                "nic-hdl:       DW-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "address:       Test address\n" +
                "e-mail:        test@ripe.net\n" +
                "source:        TEST");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "role:           Denis Walker\n" +
                "nic-hdl:        DW-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "address:        Dummy address for DW-RIPE\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST"));
        assertThat(response, containsString("remarks:        * THIS OBJECT IS MODIFIED"));
    }

    @Test
    public void mirror_query_abuse_contact() {
        databaseHelper.addObject("" +
                "role:          Denis Walker\n" +
                "nic-hdl:       DW-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "address:       Test address\n" +
                "e-mail:        test@ripe.net\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "abuse-c:        DW-RIPE\n" +
                "org-type:       OTHER\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-g TEST:3:2-2");

        assertThat(response, containsString("" +
                "organisation:   ORG1-TEST\n" +
                "org-name:       Wasp Corp\n" +
                "abuse-c:        DW-RIPE\n" +
                "org-type:       OTHER\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST"));

        assertThat(response, containsString("remarks:        * THIS OBJECT IS MODIFIED"));
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
