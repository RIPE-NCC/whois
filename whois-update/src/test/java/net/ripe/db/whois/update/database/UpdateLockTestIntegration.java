package net.ripe.db.whois.update.database;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractDaoTest;
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
public class UpdateLockTestIntegration extends AbstractDaoTest {

    private static final String MNTNER = "Dot: ";
    private final int threads = 12;
    @Autowired
    private UpdateLockHelper updateLockHelper;

    @Before
    public void setup() {
        whoisTemplate.update("INSERT INTO mntner (object_id, mntner) VALUES (1, ?)", MNTNER);
    }

    @Test
    public void test_global_update_lock() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int cnt = 0; cnt < threads; ++cnt) {
            UpdateLockWorker updateLockWorker = new UpdateLockWorker(updateLockHelper);
            executor.execute(updateLockWorker);
        }
        executor.shutdown();
        while (!executor.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
        }
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
