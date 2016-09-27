package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDaoTest {
    @Autowired JettyBootstrap jettyBootstrap;
    @Autowired protected List<ApplicationService> applicationServices;

    @Before
    public void startServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }
    }

    @After
    public void stopServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.stop(true);
        }
    }

    public int getPort() {
        return jettyBootstrap.getPort();
    }

    /**
     * This method can be called anywhere in a derived test class to
     * be able to debug the server on a local development machine.
     */
    protected synchronized void stopExecutionHereButKeepTheServerRunning() {
        Instant start = Instant.now();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                System.out.println(String.format("Server listening for %d minutes on port %d", timeElapsed.toMinutes(), getPort()));
                wait(60000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
