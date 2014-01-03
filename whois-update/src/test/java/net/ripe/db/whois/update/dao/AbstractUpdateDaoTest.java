package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-update-test.xml"})
public abstract class AbstractUpdateDaoTest extends AbstractDaoTest {
    @Autowired(required = false) protected List<ApplicationService> applicationServices = Collections.emptyList();

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
}
