package net.ripe.db.whois.query.integration;

import io.netty.channel.ChannelFuture;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SimpleKeepaliveTestIntegration extends AbstractQueryIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKeepaliveTestIntegration.class);

    private static final String END_OF_HEADER = "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n\n";
    private static final String READ_TIMEOUT_FRAGMENT = "has been closed after a period of inactivity";

    @Test
    public void kFlagShouldKeepTheConnectionOpenUntilTheSecondKWithoutArguments() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

        ChannelFuture channelFuture = client.connectAndWait();

        channelFuture.sync();

        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);

        client.sendLine("-k");
        client.waitForClose();

        assertTrue(client.getSuccess());
    }

    @Test
    public void readTimeoutShouldPrintErrorMessage() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

        ChannelFuture channelFuture = client.connectAndWait();

        channelFuture.sync();

        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);

        // Read timeout configured in @BeforeClass as 3 sec so wait at most 5
        client.waitForResponseContains(READ_TIMEOUT_FRAGMENT, 5L);
    }

    @Test
    public void kFlagShouldKeepTheConnectionOpenAfterSupportedQuery() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

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

        assertTrue(client.getSuccess());
    }


}
