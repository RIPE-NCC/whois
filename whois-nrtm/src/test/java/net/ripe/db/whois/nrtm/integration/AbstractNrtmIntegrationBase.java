package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import net.ripe.db.whois.nrtm.AccessControlList;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm-test.xml"})
public abstract class AbstractNrtmIntegrationBase extends AbstractDatabaseHelperTest {
    @Autowired protected ApplicationContext applicationContext;
    @Autowired protected NrtmServer nrtmServer;
    @Autowired protected AccessControlList accessControlList;

    @Before
    public void beforeAbstractNrtmIntegrationBase() throws Exception {
        databaseHelper.insertAclMirror("127.0.0.1/32");
        databaseHelper.insertAclMirror("0:0:0:0:0:0:0:1");
        accessControlList.reload();
    }
}
