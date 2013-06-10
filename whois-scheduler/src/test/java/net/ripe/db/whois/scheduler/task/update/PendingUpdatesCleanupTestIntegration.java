package net.ripe.db.whois.scheduler.task.update;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class PendingUpdatesCleanupTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired PendingUpdateDao pendingUpdateDao;
    @Autowired PendingUpdatesCleanup pendingUpdatesCleanup;
    @Autowired TestDateTimeProvider dateTimeProvider;

    @Test
    public void cleanup() {
        final RpslObject route = RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "descr: description\n" +
                        "origin: \n" +
                        "notify: noreply@ripe.net\n" +
                        "mnt-by: OWNER-MNT\n" +
                        "changed: noreplY@ripe.net\n" +
                        "source: TEST");

        pendingUpdateDao.store(new PendingUpdate(Sets.newHashSet("RouteAuthentication"), route, LocalDateTime.now().minusDays(8)));
        assertThat(databaseHelper.listPendingUpdates(), hasSize(1));

        pendingUpdatesCleanup.run();

        assertThat(databaseHelper.listPendingUpdates(), hasSize(0));
    }

    @Test
    public void dont_cleanup() {
        final RpslObject route = RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "descr: description\n" +
                        "origin: \n" +
                        "notify: noreply@ripe.net\n" +
                        "mnt-by: OWNER-MNT\n" +
                        "changed: noreplY@ripe.net\n" +
                        "source: TEST");

        pendingUpdateDao.store(new PendingUpdate(Sets.newHashSet("RouteAuthentication"), route, LocalDateTime.now().minusDays(6)));
        assertThat(databaseHelper.listPendingUpdates(), hasSize(1));

        pendingUpdatesCleanup.run();

        assertThat(databaseHelper.listPendingUpdates(), hasSize(1));
    }
}
