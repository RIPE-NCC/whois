package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class NrtmQueryTestIntegration extends AbstractNrtmIntegrationBase {

    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeEach
    public void before() throws InterruptedException {
        nrtmServer.start();
    }

    @AfterEach
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void versionQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-q version");

        assertThat(response, containsString("% nrtm-server"));
    }

    @Test
    public void sourcesQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-q sources");

        assertThat(response, containsString("TEST:3:X:0-0"));
        assertThat(response, containsString("TEST-NONAUTH:3:X:0-0"));
    }

    @Test
    public void emptyQuery() {
        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "\n");

        assertThat(response, containsString("no flags passed"));
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

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

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
    public void nrtm_add_objects_no_trailing_newlines() {
        databaseHelper.addObject("" +
                "role:          Alpha Bravo\n" +
                "nic-hdl:       AB-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        ab@ripe.net\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "role:          Charlie Delta\n" +
                "nic-hdl:       CD-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        cd@ripe.net\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "role:          Echo Foxtrot\n" +
                "nic-hdl:       EF-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        ef@ripe.net\n" +
                "source:        TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString(
                "ADD 1\n" +
                "\n" +
                "role:           Alpha Bravo\n" +
                "nic-hdl:        AB-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n" +
                "ADD 2\n" +
                "\n" +
                "role:           Charlie Delta\n" +
                "nic-hdl:        CD-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n" +
                "ADD 3\n" +
                "\n" +
                "role:           Echo Foxtrot\n" +
                "nic-hdl:        EF-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n"));
    }

    @Test
    public void nrtm_add_objects_with_trailing_newlines() {
        databaseHelper.addObject("" +
                "role:          Alpha Bravo\n" +
                "nic-hdl:       AB-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        ab@ripe.net\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "role:          Charlie Delta\n" +
                "nic-hdl:       CD-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        cd@ripe.net\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "role:          Echo Foxtrot\n" +
                "nic-hdl:       EF-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "e-mail:        ef@ripe.net\n" +
                "source:        TEST\n");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString(
                "ADD 1\n" +
                "\n" +
                "role:           Alpha Bravo\n" +
                "nic-hdl:        AB-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n" +
                "ADD 2\n" +
                "\n" +
                "role:           Charlie Delta\n" +
                "nic-hdl:        CD-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n" +
                "ADD 3\n" +
                "\n" +
                "role:           Echo Foxtrot\n" +
                "nic-hdl:        EF-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n"));
    }

    @Test
    public void nrtm_latin1_umlaut_descr_remarks_not_substituted() {
        databaseHelper.addObject("" +
                "role:          Denis Walker\n" +
                "nic-hdl:       DW-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "descr:         Bratwürst\n" +
                "remarks:       Bratwürst\n" +
                "e-mail:        test@ripe.net\n" +
                "created:       2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "source:        TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "role:           Denis Walker\n" +
                "nic-hdl:        DW-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "descr:          Bratwürst\n" +
                "remarks:        Bratwürst\n" +
                "e-mail:         unread@ripe.net\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST"));
        assertThat(response, containsString("remarks:        * THIS OBJECT IS MODIFIED"));
    }

    @Test
    public void nrtm_utf8_descr_remarks_substituted() {
        databaseHelper.addObject("" +
                "role:          Denis Walker\n" +
                "nic-hdl:       DW-RIPE\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "descr:         Т͡хе кви́к браун фокс джампс оувѐр тхе лэйзи дог.\n" +
                "remarks:       Т͡хе кви́к браун фокс джампс оувѐр тхе лэйзи дог.\n" +
                "e-mail:        test@ripe.net\n" +
                "created:       2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "source:        TEST");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-LAST");

        assertThat(response, containsString("" +
                "role:           Denis Walker\n" +
                "nic-hdl:        DW-RIPE\n" +
                "abuse-mailbox:  abuse@ripe.net\n" +
                "descr:          ???? ????? ????? ???? ?????? ?????? ??? ????? ???.\n" +
                "remarks:        ???? ????? ????? ???? ?????? ?????? ??? ????? ???.\n" +
                "e-mail:         unread@ripe.net\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "source:         TEST"));
        assertThat(response, containsString("remarks:        * THIS OBJECT IS MODIFIED"));
    }

    @Test
    public void should_not_have_blank_lines_between_sources() {
        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-q sources");

        assertThat(response, containsString(
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                        "% See https://docs.db.ripe.net/terms-conditions.html\n" +
                        "\n" +
                        "TEST:3:X:0-0\n" +
                        "TEST-NONAUTH:3:X:0-0\n" +
                        "\n"
        ));
    }

    @Test
    public void display_acl_blocked_error() {
        try {
            databaseHelper.insertAclIpDenied("127.0.0.1/32");
            ipResourceConfiguration.reload();

            final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-q sources");

            assertThat(response, containsString("ERROR:201: access denied"));
        } finally {
            databaseHelper.unbanIp("127.0.0.1/32");
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

}
