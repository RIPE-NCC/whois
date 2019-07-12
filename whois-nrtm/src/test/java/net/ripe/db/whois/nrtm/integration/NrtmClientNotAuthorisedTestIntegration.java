package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.nrtm.AccessControlList;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientNotAuthorisedTestIntegration extends AbstractNrtmIntegrationBase {

    @Autowired protected NrtmServer nrtmServer;
    @Autowired protected NrtmImporter nrtmImporter;
    @Autowired protected AccessControlList accessControlList;

    @BeforeClass
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @Before
    public void before() {
        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.getPort()));
    }

    @Test
    public void nrtm_client_not_authorised() {
        databaseHelper.clearAclMirrors();
        accessControlList.reload();

        try {
            nrtmImporter.start();
            Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(() -> countThreads("NrtmClient") > 0);
        } finally {
            nrtmImporter.stop(false);
        }
    }
}
