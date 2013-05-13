package net.ripe.db.whois.update.database;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractDaoTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class UpdateLockTestIntegration extends AbstractDaoTest implements Runnable {

    private static final String MNTNER = "Dot: ";

    private final int threads = 12;
    private final CountDownLatch completed = new CountDownLatch(threads);

    @Autowired private UpdateLockHelper updateLockHelper;

    @Before
    public void setup() {
        whoisTemplate.update("INSERT INTO mntner (object_id, mntner) VALUES (1, ?)", MNTNER);
    }

    @Test
    public void test_global_update_lock() throws Exception {

        for (int i = 0; i < threads; i++) {
            final Thread thread = new Thread(this);
            thread.start();
        }

        completed.await(10, TimeUnit.SECONDS);

        assertThat(getMntnerValue(), is("Dot: " + StringUtils.repeat(".", threads)));
    }

    private String getMntnerValue() {
        return whoisTemplate.queryForObject("SELECT mntner FROM mntner WHERE object_id = 1", String.class);
    }

    @Override
    public void run() {
        updateLockHelper.testUpdateLock();
        completed.countDown();
    }
}
