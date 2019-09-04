package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.AccessControlList;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm-test.xml"})
public abstract class AbstractNrtmIntegrationBase extends AbstractDatabaseHelperIntegrationTest {
    @Autowired protected NrtmServer nrtmServer;
    @Autowired protected AccessControlList accessControlList;
    @Autowired protected SourceContext sourceContext;

    @Before
    public void beforeAbstractNrtmIntegrationBase() {
        databaseHelper.insertAclMirror("127.0.0.1/32");
        databaseHelper.insertAclMirror("0:0:0:0:0:0:0:1");
        accessControlList.reload();
    }

    protected void objectExists(final ObjectType type, final String key, final boolean exists) {
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
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

    protected long countThreads(final String prefix) {
        return getAllThreads()
                .stream()
                .filter(thread -> thread.getName().startsWith(prefix))
                .count();
    }

    private Set<Thread> getAllThreads() {
        return Thread.getAllStackTraces().keySet();
    }

}
