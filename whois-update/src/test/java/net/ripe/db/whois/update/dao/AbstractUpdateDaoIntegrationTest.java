package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-update-test.xml"})
public abstract class AbstractUpdateDaoIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired(required = false) protected List<ApplicationService> applicationServices = Collections.emptyList();

    @BeforeEach
    public void startServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }
    }

    @AfterEach
    public void stopServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.stop(true);
        }
    }
}
