package net.ripe.db.whois.internal;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("TEST")
@TestExecutionListeners(listeners = {
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
}, inheritListeners = false)
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"})
public abstract class AbstractInternalTest extends AbstractJUnit4SpringContextTests {
    protected static final String apiKey = "DB-WHOIS-testapikey";

    @Autowired protected ApplicationContext applicationContext;
    @Autowired protected DateTimeProvider dateTimeProvider;

    @Autowired @Qualifier("aclDataSource") protected DataSource aclDataSource;

    @Autowired JettyBootstrap jettyBootstrap;

    protected DatabaseHelper databaseHelper;

    @BeforeClass
    public synchronized static void setupAbstractDatabaseHelperTest() throws Exception {
        Slf4JLogConfiguration.init();
        DatabaseHelper.setupDatabase();
    }

    @Before
    public void setDatabaseHelper() {
        databaseHelper = new DatabaseHelper();      // TODO: [AH] DatabaseHelper is waaay overloaded, split into smaller parts so it can be used here easily, too
        databaseHelper.setAclDataSource(aclDataSource);
    }

    @After
    public void resetDatabaseHelper() {
        databaseHelper.setupAclDatabase();
    }

    @Before
    public void startServer() throws Exception {
        jettyBootstrap.start();
    }

    @After
    public void stopServer() throws Exception {
        jettyBootstrap.stop(true);
    }

    public int getPort() {
        return jettyBootstrap.getPort();
    }
}
