package net.ripe.db.whois.nrtm.integration;

import com.google.common.collect.Lists;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientACLLimitTestIntegration extends AbstractNrtmIntegrationBase {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "source: TEST");

    @Autowired
    protected NrtmImporter nrtmImporter;
    @Autowired
    protected SourceContext sourceContext;

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
        databaseHelper.addObject(MNTNER);
        databaseHelper.addObjectToSource("1-GRS", MNTNER);

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.getPort()));
    }

    @After
    public void reset() {
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void acl_denied() throws Exception {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();
        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT1\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);

        try {
            nrtmImporter.start();
            objectExists(ObjectType.MNTNER, "TEST-MNT1", false);
        } finally {
            nrtmImporter.stop(false);
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
        }
    }

    @Test
    public void acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);

        try {
            accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);
            nrtmImporter.start();
            objectExists(ObjectType.MNTNER, "TEST-MNT", false);

        } finally {
            nrtmImporter.stop(false);
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void acl_limit_not_breached() throws Exception {
        databaseHelper.unban(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();

        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT2\n" +
                "source: TEST");
        databaseHelper.addObject(mntner);

        nrtmImporter.start();
        objectExists(ObjectType.MNTNER, "TEST-MNT2", true);
    }
}
