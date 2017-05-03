package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.kubek2k.springockito.annotations.WrapWithSpy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-query-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class WhoisShutdownTestIntegration extends AbstractQueryIntegrationTest {
    @Autowired @WrapWithSpy private QueryChannelsRegistry queryChannelsRegistry;

    @Before
    public void setUp() throws Exception {
        queryServer.start();
    }

    @After
    public void tearDown() throws Exception {
        queryServer.stop(true);
    }

    @Test
    public void shouldShutdownWithOpenClientConnection() throws Exception {
        Socket socket = new Socket(HOST, QueryServer.port);
        try {
            assertTrue("server connection", socket.isConnected());
            assertTrue("header from server", socket.getInputStream().read() != -1);

            assertEquals("single client connection", 1, queryChannelsRegistry.size());

            final CountDownLatch latch = new CountDownLatch(1);
            new Thread() {
                @Override
                public void run() {
                    queryServer.stop(true);
                    latch.countDown();
                }
            }.start();

            if (!latch.await(2500, TimeUnit.MILLISECONDS)) {
                fail("Server did not shutdown with open client connection.");
            }
        } finally {
            socket.close();
        }
    }
}
