package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import java.net.InetAddress;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientAclLimitTestIntegration extends AbstractNrtmIntegrationBase {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    private static final RpslObject mntner = RpslObject.parse("" +
            "mntner: TEST-MNT\n" +
            "source: TEST");

    @Autowired
    protected NrtmImporter nrtmImporter;

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeClass
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @Before
    public void before() {
        databaseHelper.addObject(mntner);

        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.getPort()));
    }

    @After
    public void reset() {
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");

        nrtmImporter.stop(true);
        nrtmServer.stop(true);
    }

    @Test
    public void acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);

        accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);
        nrtmImporter.start();
        objectExists(ObjectType.MNTNER, "TEST-MNT", false);
    }

    @Test
    public void acl_denied()  {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();

        nrtmImporter.start();
        objectExists(ObjectType.MNTNER, "TEST-MNT", false);
    }

    @Test
    public void acl_limit_not_breached() throws Exception {
        nrtmImporter.start();
        objectExists(ObjectType.MNTNER, "TEST-MNT", true);
    }
}
