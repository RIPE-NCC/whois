package net.ripe.db.whois.internal;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.internal.api.rnd.dao.JdbcObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.dao.JdbcObjectReferenceUpdateDao;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceUpdateDao;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(WhoisProfile.TEST)
@TestExecutionListeners(listeners = {TransactionalTestExecutionListener.class})
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"})
public abstract class AbstractInternalTest extends AbstractJUnit4SpringContextTests {
    protected static final String apiKey = "DB-WHOIS-testapikey";

    @Autowired protected TestDateTimeProvider testDateTimeProvider;

    @Autowired @Qualifier("aclDataSource") protected DataSource aclDataSource;
    @Autowired @Qualifier("whoisReadOnlySlaveDataSource") protected DataSource whoisDataSource;
    @Autowired @Qualifier("whoisUpdateMasterDataSource") protected DataSource whoisUpdateDataSource;
    @Autowired protected SourceAwareDataSource sourceAwareDataSource;

    @Autowired JettyBootstrap jettyBootstrap;
    protected JdbcRpslObjectUpdateDao updateDao;
    protected ObjectReferenceDao objectReferenceDao;
    protected ObjectReferenceUpdateDao objectReferenceUpdateDao;
    protected VersionDao versionDao;

    protected DatabaseHelper databaseHelper;
    protected JdbcTemplate whoisTemplate;


    @BeforeClass
    public synchronized static void setupAbstractDatabaseHelperTest() throws Exception {
        Slf4JLogConfiguration.init();
        DatabaseHelper.setupDatabase();
    }

    @Before
    public void setDatabaseHelper() {
        databaseHelper = new DatabaseHelper();
        databaseHelper.setAclDataSource(aclDataSource);

        updateDao = new JdbcRpslObjectUpdateDao(whoisUpdateDataSource, testDateTimeProvider);
        objectReferenceDao = new JdbcObjectReferenceDao(sourceAwareDataSource);
        objectReferenceUpdateDao = new JdbcObjectReferenceUpdateDao(whoisUpdateDataSource);
        versionDao = new JdbcVersionDao(sourceAwareDataSource);

        whoisTemplate = new JdbcTemplate(whoisUpdateDataSource);
        setupWhoisDatabase(whoisTemplate);
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

    public void setupWhoisDatabase(JdbcTemplate jdbcTemplate) {
        truncateTables(jdbcTemplate);
        loadScripts(jdbcTemplate, "whois_data.sql");
    }
}
