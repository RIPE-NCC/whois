package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.kubek2k.springockito.annotations.WrapWithSpy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-query-test.xml"}, inheritLocations = false)
@Tag("IntegrationTest")
public class WhoisShutdownTestIntegration extends AbstractQueryIntegrationTest {
    @Autowired @WrapWithSpy private QueryChannelsRegistry queryChannelsRegistry;

    @BeforeEach
    public void setUp() throws Exception {
        queryServer.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        queryServer.stop(true);
    }

    @Test
    public void shouldShutdownWithOpenClientConnection() throws Exception {
        final Socket socket = new Socket(HOST, QueryServer.port);
        try {
            assertThat(socket.isConnected(), is(true)); // server connection
            assertThat(socket.getInputStream().read(), is(not(-1))); // header from server
            assertThat(queryChannelsRegistry.size(), is(1));    // single client connection

            final CountDownLatch latch = new CountDownLatch(1);
            new Thread() {
                @Override
                public void run() {
                    queryServer.stop(true);
                    latch.countDown();
                }
            }.start();

            if (!latch.await(2500L, TimeUnit.MILLISECONDS)) {
                fail("Server did not shutdown with open client connection.");
            }
        } finally {
            socket.close();
        }
    }
}
