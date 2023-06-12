package net.ripe.db.whois.api;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired JettyBootstrap jettyBootstrap;
    @Autowired protected List<ApplicationService> applicationServices;

    @BeforeEach
    public void startServer() {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }
    }

    protected void setTime(final LocalDateTime localDateTime){
        testDateTimeProvider.setTime(localDateTime);
    }

    @AfterEach
    public void stopServer() {
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

        while (true) {
            Duration timeElapsed = Duration.between(start, Instant.now());
            System.out.println(String.format("Server listening for %d minutes on port %d", timeElapsed.toMinutes(), getPort()));
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
        }
    }
}
