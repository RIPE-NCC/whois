package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;

@ActiveProfiles("TEST")
@TestExecutionListeners(listeners = {SetupDatabaseTestExecutionListener.class, TransactionalTestExecutionListener.class})
public abstract class AbstractDatabaseHelperTest extends AbstractJUnit4SpringContextTests {
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

    @BeforeClass
    public static void setupProperties() {
        Slf4JLogConfiguration.init();
        System.setProperty("mail.dequeue.interval", "10");
        System.setProperty("application.version", "0.1-TEST");
    }

    @Before
    public void resetStubs() throws Exception {
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
