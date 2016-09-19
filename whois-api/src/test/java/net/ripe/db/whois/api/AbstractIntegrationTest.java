package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.api.rest.YieldToTestServer;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDaoTest {
    @Autowired JettyBootstrap jettyBootstrap;
    @Autowired protected List<ApplicationService> applicationServices;

    @Before
    public void startServer() throws Exception {
        jettyBootstrap.setPort(42300);
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

    protected synchronized void stopTestHereButKeepLocalServerRunning() {
        YieldToTestServer.yield(this);
    }
}
