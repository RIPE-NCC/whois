package net.ripe.db.whois.scheduler;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-scheduler-test.xml"})
public abstract class AbstractSchedulerIntegrationTest extends AbstractDatabaseHelperIntegrationTest {
    @Autowired protected QueryServer queryServer;
    @Autowired protected IpResourceConfiguration ipResourceConfiguration;

    @BeforeEach
    public final void setUpAbstractIntegrationTest() throws Exception {
        databaseHelper.clearAclLimits();
        databaseHelper.insertAclIpLimit("0/0", -1, true);
        databaseHelper.insertAclIpLimit("::0/0", -1, true);
        ipResourceConfiguration.reload();
    }

    @BeforeAll
    public static void enableGrsImport() {
        System.setProperty("grs.import.enabled", "true");
    }

    @AfterAll
    public static void clearGrsImport() {
        System.clearProperty("grs.import.enabled");
    }

}
