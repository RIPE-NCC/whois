package net.ripe.db.whois.common.profiles.usingspring;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// Cant use activeprofiles because it interferes with static intialised "spring.profiles.active"
// /@ActiveProfiles("TEST")
@TestExecutionListeners(listeners = {TransactionalTestExecutionListener.class})
public abstract class ModifiedAbstractDatabaseHelperTest extends AbstractJUnit4SpringContextTests {
    @Autowired protected ApplicationContext applicationContext;
    @Autowired protected SourceContext sourceContext;
    @Autowired protected DateTimeProvider dateTimeProvider;
    @Autowired protected RpslObjectDao rpslObjectDao;
    @Autowired protected RpslObjectUpdateDao rpslObjectUpdateDao;
    @Autowired protected SourceAwareDataSource sourceAwareDataSource;
    @Autowired protected IpTreeUpdater ipTreeUpdater;
    @Autowired protected List<Stub> stubs;

    @Value("${whois.source}") protected String source;

    protected JdbcTemplate whoisTemplate;
    protected JdbcTemplate mailUpdatesTemplate;
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
        System.setProperty("grs.import.test.resourceDataUrl", new ClassPathResource("/grs/delegated-test").getURL().toString());
    }

    @AfterClass
    public synchronized static void resetAbstractDatabaseHelperTest() throws Exception {
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(propertyStore));
        System.setProperties(properties);
        propertyStore = null;
    }

    @Before
    public void resetStubs() throws Exception {
        databaseHelper.setup();

        for (final Stub stub : stubs) {
            stub.reset();
        }

        ipTreeUpdater.rebuild();
    }

    @Autowired
    public void setDatabaseHelper(final DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.whoisTemplate = databaseHelper.getWhoisTemplate();
        this.mailUpdatesTemplate = new JdbcTemplate(databaseHelper.getMailupdatesDataSource());
    }
}
