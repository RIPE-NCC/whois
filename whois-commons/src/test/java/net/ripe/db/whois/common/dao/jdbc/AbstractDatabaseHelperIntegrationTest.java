package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;

// TODO: [AH] remove mandatory @DIrtiesContext, rely on per-class @DirtiesContext and ReinitTestExecutionListener instead
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(WhoisProfile.TEST)
@TestExecutionListeners(listeners = {TransactionalTestExecutionListener.class})
public abstract class AbstractDatabaseHelperIntegrationTest extends AbstractJUnit4SpringContextTests {
    @Autowired protected TestDateTimeProvider testDateTimeProvider;
    @Autowired protected List<Stub> stubs;

    protected JdbcTemplate whoisTemplate;
    protected JdbcTemplate internalsTemplate;
    protected DatabaseHelper databaseHelper;

    private static byte[] propertyStore = null;

    @BeforeClass
    public synchronized static void setupAbstractDatabaseHelperTest() throws Exception {
        DatabaseHelper.setupDatabase();
        Slf4JLogConfiguration.init();

        if (propertyStore == null) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.getProperties().store(out, "AbstractDatabaseHelperTest");
            propertyStore = out.toByteArray();
        }

        System.setProperty("mail.dequeue.interval", "3");
        System.setProperty("application.version", "0.1-TEST");
        System.setProperty("grs.sources", "TEST-GRS");
        System.setProperty("grs.sources.dummify", "TEST-GRS");
        System.setProperty("api.rest.baseurl", "http://rest-test.db.ripe.net");

        // default commit-id
        System.setProperty("git.commit.id.abbrev", "0");

        // allow setting Origin header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @AfterClass
    public synchronized static void resetAbstractDatabaseHelperTest() throws Exception {
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(propertyStore));
        System.setProperties(properties);
        propertyStore = null;
    }

    @Before
    public void resetDatabaseHelper() throws Exception {
        databaseHelper.setup();
    }

    @Before
    public void resetStubs() {
        for (final Stub stub : stubs) {
            stub.reset();
        }
    }

    @Autowired
    public void setDatabaseHelper(final DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.whoisTemplate = databaseHelper.getWhoisTemplate();
        this.internalsTemplate = databaseHelper.getInternalsTemplate();
    }
}
