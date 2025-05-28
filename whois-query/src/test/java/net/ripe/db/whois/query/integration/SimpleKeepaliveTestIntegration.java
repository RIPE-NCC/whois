package net.ripe.db.whois.query.integration;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelFuture;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SimpleKeepaliveTestIntegration extends AbstractQueryIntegrationTest {

    private static final String END_OF_HEADER = "% See https://docs.db.ripe.net/terms-conditions.html\n\n";
    private static final String READ_TIMEOUT_FRAGMENT = "has been closed after a period of inactivity";

    @BeforeAll
    public static void setProperty() {
        System.setProperty("whois.read.timeout.sec", "3");
    }

    @AfterAll
    public static void clearProperty() {
        System.clearProperty("whois.read.timeout.sec");
    }

    // TODO: [AH] most tests don't taint the DB; have a 'tainted' flag in DBHelper, reinit only if needed
    @BeforeEach
    public void startupWhoisServer() {
        final RpslObject person = RpslObject.parse("person: ADM-TEST\naddress: address\nphone: +312342343\nmnt-by:RIPE-NCC-HM-MNT\nadmin-c: ADM-TEST\nnic-hdl: ADM-TEST\nsource: TEST");
        final RpslObject mntner = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\ndescr: description\nadmin-c: ADM-TEST\nsource: TEST");
        databaseHelper.addObjects(Lists.newArrayList(person, mntner));

        databaseHelper.addObject("inetnum: 81.0.0.0 - 82.255.255.255\nnetname: NE\nmnt-by:RIPE-NCC-HM-MNT\nsource: TEST");
        databaseHelper.addObject("domain: 117.80.81.in-addr.arpa\nsource: TEST");
        databaseHelper.addObject("inetnum: 81.80.117.237 - 81.80.117.237\nnetname: NN\nstatus: OTHER\nsource: TEST");
        databaseHelper.addObject("route: 81.80.117.0/24\norigin: AS123\n");
        databaseHelper.addObject("route: 81.80.0.0/16\norigin: AS123\n");
        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @AfterEach
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void kFlagShouldKeepTheConnectionOpenUntilTheSecondKWithoutArguments() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(queryServer.getPort());

        ChannelFuture channelFuture = client.connectAndWait();

        channelFuture.sync();

        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);

        client.sendLine("-k");
        client.waitForClose();

        assertThat(client.getSuccess(), is(true));
    }

    @Test
    public void readTimeoutShouldPrintErrorMessage() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(queryServer.getPort());

        ChannelFuture channelFuture = client.connectAndWait();

        channelFuture.sync();

        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);

        // Read timeout configured in @BeforeClass as 3 sec so wait at most 5
        client.waitForResponseContains(READ_TIMEOUT_FRAGMENT, 5L);
    }

    @Test
    public void kFlagShouldKeepTheConnectionOpenAfterSupportedQuery() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(queryServer.getPort());

        ChannelFuture channelFuture = client.connectAndWait();
        channelFuture.sync();

        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);
        client.clearBuffer();

        client.sendLine("-rBGxTinetnum 81.80.117.237 - 81.80.117.237");
        client.waitForResponseEndsWith(END_OF_HEADER);

        assertThat(client.getResponse(), containsString("inetnum:        81.80.117.237 - 81.80.117.237"));

        client.sendLine("-k");
        client.waitForClose();

        assertThat(client.getSuccess(), is(true));
    }


}
