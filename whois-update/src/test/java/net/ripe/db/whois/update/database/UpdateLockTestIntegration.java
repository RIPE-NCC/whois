package net.ripe.db.whois.update.database;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class UpdateLockTestIntegration extends AbstractUpdateDaoTest {

    private static final String MNTNER = "Dot: ";
    private final int threads = 12;
    @Autowired
    private UpdateLockHelper updateLockHelper;

    @Before
    public void setup() {
        whoisTemplate.update("INSERT INTO mntner (object_id, mntner) VALUES (1, ?)", MNTNER);
    }

    /* We are checking for 2 very important requirements regarding SQL access:
    1. Global update lock works, there is only 1 thread at the same time writing to the database;
    2. Transaction isolation level READ_COMMITTED allows us to read before setting the global update lock

    The latter is very important as innodb, when using the default isolation level REPEATABLE READ,
    creates a snapshot of the DB when executing the first query in a transaction. This means all successive reads will
    read the snapshot, but writes will update the actual database. Switching to READ_COMMITTED will abandon this and will
    allow us to see all the commits that happened since the first SELECT statement in the transaction.

    e.g.: imagine executing a few SELECTs before acquiring the global update lock normally would mean that a commit going through
    between the first select and the global lock ownership are NOT visible for the rest of the transaction, a.k.a. business rule
    validators!
     */

    @Test
    public void test_global_update_lock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int cnt = 0; cnt < threads; ++cnt) {
            UpdateLockWorker updateLockWorker = new UpdateLockWorker(updateLockHelper);
            executor.execute(updateLockWorker);
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS), is(true));
        assertThat(getMntnerValue(), is("Dot: " + StringUtils.repeat(".", threads)));
    }

    private String getMntnerValue() {
        return whoisTemplate.queryForObject("SELECT mntner FROM mntner WHERE object_id = 1", String.class);
    }

    private static class UpdateLockWorker implements Runnable {
        UpdateLockHelper updateLockHelper;

        UpdateLockWorker(UpdateLockHelper updateLockHelper) {
            this.updateLockHelper = updateLockHelper;
        }

        @Override
        public void run() {
            updateLockHelper.testUpdateLock();
        }
    }
}
