package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class NrtmMirrorQueryTestIntegration extends AbstractNrtmIntegrationBase {

    @BeforeEach
    public void before() throws InterruptedException {
        nrtmServer.start();
    }

    @AfterEach
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void mirrorQueryOneSerialEntry() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-1");

        assertThat(response, containsString("aut-num:        AS4294967207"));
    }

    @Test
    public void mirrorQueryOneSerialEntryNonAuth() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST-NONAUTH");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST-NONAUTH:3:1-1");

        assertThat(response, containsString("aut-num:        AS4294967207"));
    }

    @Test
    public void mirrorQueryMultipleSerialEntry() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-3");

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

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-3");

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

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST-NONAUTH:3:1-3");

        assertThat(response, containsString("ADD 2"));
        assertThat(response, containsString("AS5294967207"));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("AS6294967207"));
        assertThat(response, containsString("%END TEST-NONAUTH"));
        assertThat(response, not(containsString("DW-RIPE")));
    }


    @Test
    public void mirrorQueryOutofRange() {
        databaseHelper.addObject("aut-num:AS4294967207");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:2-4");

        assertThat(response, containsString("invalid range: Not within 1-1"));
    }

    @Test
    public void mirrorQueryWithLastKeyword() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT\nsource:TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

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

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

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

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:2-2");

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

}
