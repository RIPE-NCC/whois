package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.WhoisNrtmTestConfiguration;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = WhoisNrtmTestConfiguration.class)
public abstract class AbstractNrtmIntegrationBase extends AbstractDatabaseHelperIntegrationTest {
    @Autowired protected NrtmServer nrtmServer;
    @Autowired protected SourceContext sourceContext;

    protected void objectExists(final ObjectType type, final String key, final boolean exists) {
        Awaitility.waitAtMost(5L, TimeUnit.SECONDS).until(new Callable<Boolean>() {
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
