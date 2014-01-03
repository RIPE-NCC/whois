package net.ripe.db.whois.scheduler.task.update;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.internet.MimeMessage;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-scheduler-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class PendingUpdatesCleanupTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired PendingUpdateDao pendingUpdateDao;
    @Autowired MailSenderStub mailSenderStub;
    @Autowired @ReplaceWithMock UpdateLog updateLog;
    @Autowired PendingUpdatesCleanup subject;

    @Before
    public void setup() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:      OWNER-MNT\n" +
                "upd-to:      dbtest@ripe.net\n" +
                "source:      TEST"));
    }

    @Test
    public void cleanup_one_expired_update() throws Exception {
        final RpslObject route = RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                "descr: description\n" +
                "origin: AS123\n" +
                "notify: noreply@ripe.net\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: noreplY@ripe.net\n" +
                "source: TEST");

        pendingUpdateDao.store(new PendingUpdate(Sets.newHashSet("RouteAuthentication"), route, LocalDateTime.now().minusDays(8)));
        assertThat(getPendingUpdateCount(), is(1));

        subject.run();

        verify(updateLog).logUpdateResult(any(UpdateRequest.class), any(UpdateContext.class), any(Update.class), any(Stopwatch.class));
        final MimeMessage message = mailSenderStub.getMessage("dbtest@ripe.net");
        assertThat(message.getContent().toString(), containsString("NO FINAL CREATE REQUESTED FOR"));
        assertThat(getPendingUpdateCount(), is(0));
    }

    @Test
    public void dont_cleanup_pending_update() {
        final RpslObject route = RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                "descr: description\n" +
                "origin: AS123\n" +
                "notify: noreply@ripe.net\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: noreplY@ripe.net\n" +
                "source: TEST");
        pendingUpdateDao.store(new PendingUpdate(Sets.newHashSet("RouteAuthentication"), route, LocalDateTime.now().minusDays(6)));
        assertThat(getPendingUpdateCount(), is(1));

        subject.run();

        verify(updateLog, never()).logUpdateResult(any(UpdateRequest.class), any(UpdateContext.class), any(Update.class), any(Stopwatch.class));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
        assertThat(getPendingUpdateCount(), is(1));
    }

    private int getPendingUpdateCount() {
        return databaseHelper.getInternalsTemplate().queryForInt("SELECT count(*) FROM pending_updates");
    }
}
