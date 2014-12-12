package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.AccessControlList;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm-test.xml"})
public abstract class AbstractNrtmIntegrationBase extends AbstractDatabaseHelperTest {
    @Autowired protected NrtmServer nrtmServer;
    @Autowired protected AccessControlList accessControlList;
    @Autowired protected SourceContext sourceContext;

    @Before
    public void beforeAbstractNrtmIntegrationBase() throws Exception {
        databaseHelper.insertAclMirror("127.0.0.1/32");
        databaseHelper.insertAclMirror("0:0:0:0:0:0:0:1");
        accessControlList.reload();
    }

    protected void objectExists(final ObjectType type, final String key, final boolean exists) {
        Awaitility.waitAtMost(Duration.FOREVER).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    sourceContext.setCurrent(Source.master("1-GRS"));
                    databaseHelper.lookupObject(type, key);
                    return Boolean.TRUE;
                } catch (EmptyResultDataAccessException e) {
                    return Boolean.FALSE;
                } finally {
                    sourceContext.removeCurrentSource();
                }
            }
        }, is(exists));
    }
}
