package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class NrtmKeepaliveQueryTestIntegration extends AbstractNrtmIntegrationBase {

    @Value("${nrtm.update.interval:15}") private String updateIntervalString;

    private int updateInterval;

    @BeforeEach
    public void before() throws InterruptedException {
        updateInterval = Integer.valueOf(updateIntervalString);
        nrtmServer.start();
    }

    @AfterEach
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void queryKeepaliveNoPreExistingObjects() {
        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST:3:1-2 -k", (updateInterval + 1) * 1000);

        assertThat(response, containsString("%ERROR:401: invalid range"));
    }

    @Test
    public void queryKeepAliveNoPreExistingObjectsOneNewObject() {
        databaseHelper.addObject(RpslObject.parse("mntner:test\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(nrtmServer.getPort(), "-g TEST:3:1-1 -k", (updateInterval + 1));

        client.start();
        databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST"));
        String response = client.end();

        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveNoPreExistingObjectsOneNewObject2() {
        databaseHelper.addObject(RpslObject.parse("mntner:test\nsource:TEST-NONAUTH"));
        AsyncNrtmClient client = new AsyncNrtmClient(nrtmServer.getPort(), "-g TEST-NONAUTH:3:1-1 -k", (updateInterval + 1));

        client.start();
        databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST-NONAUTH"));
        String response = client.end();

        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveOnePreExistingObjectsOneNewObject() {
        databaseHelper.addObject(RpslObject.parse("mntner:testmntner\nmnt-by:testmntner\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(nrtmServer.getPort(), "-g TEST:3:1-LAST -k", (updateInterval + 1));

        client.start();
        super.databaseHelper.addObject(RpslObject.parse("mntner:keepalive\nsource:TEST"));
        String response = client.end();

        assertThat(response, containsString("mntner:         testmntner"));
        assertThat(response, containsString("mntner:         keepalive"));
    }

    @Test
    public void queryKeepAliveOnePreExistingObjectDifferentSource() {
        databaseHelper.addObject(RpslObject.parse("mntner:testmntner\nmnt-by:testmntner\nsource:TEST"));
        AsyncNrtmClient client = new AsyncNrtmClient(nrtmServer.getPort(), "-g TEST-NONAUTH:3:1-LAST -k", (updateInterval + 1));

        client.start();
        //super.databaseHelper.addObject(RpslObject.parse("aut-num:AS4294967207\nsource:TEST-NONAUTH"));
        String response = client.end();

        assertThat(response, not(containsString("testmntner")));
        //assertThat(response, not(containsString("AS4294967207")));
    }

    @Test
    public void queryKeepAliveMultipleSerialEntryMixedSource() {
        databaseHelper.addObject("aut-num:AS4294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS5294967207\nsource:TEST");
        databaseHelper.addObject("aut-num:AS6294967207\nsource:TEST-NONAUTH");
        databaseHelper.addObject("aut-num:AS7294967207\nsource:TEST-NONAUTH");

        final String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), "-g TEST-NONAUTH:3:1-LAST");

        assertThat(response, containsString("ADD 1"));
        assertThat(response, containsString("AS4294967207"));
        assertThat(response, containsString("ADD 3"));
        assertThat(response, containsString("AS6294967207"));
        assertThat(response, containsString("ADD 4"));
        assertThat(response, containsString("AS7294967207"));
        assertThat(response, containsString("source:         TEST-NONAUTH"));
        assertThat(response, containsString("%END TEST-NONAUTH"));
    }


}
