package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.JettyConfig;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDatabaseHelperTest {
    @Autowired protected JettyConfig jettyConfig;
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

    public int getPort(final Audience audience) {
        return jettyConfig.getPort(audience);
    }
}
