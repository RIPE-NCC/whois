package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-update-test.xml"})
@Transactional(propagation = Propagation.NEVER)
public abstract class AbstractUpdateDaoIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired(required = false) protected List<ApplicationService> applicationServices = Collections.emptyList();

    @BeforeEach
    public void startServer() {
        for (final ApplicationService applicationService : applicationServices) {
            try {
                applicationService.start();
            } catch (IllegalTransactionStateException illegalTransactionStateException){
                System.out.println("Service already working");
            }
        }
    }

    @AfterEach
    public void stopServer() {
        for (final ApplicationService applicationService : applicationServices) {
            try {
                applicationService.stop(true);
            } catch (IllegalTransactionStateException illegalTransactionStateException){
                System.out.println("Service already stopped");
            }
        }
    }
}
