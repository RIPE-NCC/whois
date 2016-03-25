package net.ripe.db.whois.changedphase3;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.changedphase3.util.Context;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.nrtm.AccessControlList;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.scheduler.task.export.DatabaseTextExport;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static net.ripe.db.whois.RpslObjectFixtures.OWNER_MNT;
import static net.ripe.db.whois.RpslObjectFixtures.TEST_PERSON;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-endtoend-test.xml"})
public abstract class AbstractChangedPhase3IntegrationTest extends AbstractIntegrationTest {

    @Autowired protected AccessControlList accessControlList;
    protected Context context;
    @Autowired private MaintenanceMode maintenanceMode;
    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired private MailSenderStub mailSenderStub;
    @Autowired private NrtmServer nrtmServer;
    @Autowired private DatabaseTextExport databaseTextExport;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("nrtm.enabled", "true");
    }

    @BeforeClass
    public static void afterClass() {
        System.clearProperty("nrtm.enabled");
    }

    @Before
    public void setup() {
        // Allow nrtm from localhost
        databaseHelper.insertAclMirror("127.0.0.1/32");
        databaseHelper.insertAclMirror("0:0:0:0:0:0:0:1");
        accessControlList.reload();

        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        maintenanceMode.set("FULL,FULL");
        context = new Context(getPort(), getPort(), whoisObjectMapper, mailUpdatesTestSupport, mailSenderStub,
                nrtmServer, databaseHelper, databaseTextExport);
    }

    @After
    public void teardown() {
    }
}
