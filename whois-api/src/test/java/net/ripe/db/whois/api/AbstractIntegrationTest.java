package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.sso.CrowdClient;
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
    final CrowdServerDummy crowdServerDummy = new CrowdServerDummy();

    @Before
    public void startServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }

        crowdServerDummy.start();
        applicationContext.getBean(CrowdClient.class).setRestUrl(String.format("http://localhost:%s/crowd", crowdServerDummy.getPort()));
    }

    @After
    public void stopServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.stop(true);
        }
        crowdServerDummy.stop();
    }

    public int getPort() {
        return jettyBootstrap.getPort();
    }
}
