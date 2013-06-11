package net.ripe.db.whois.scheduler.task.update;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.mail.MailGateway;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PendingUpdatesCleanupTest {
    @Mock private PendingUpdateDao pendingUpdateDao;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private DateTimeProvider dateTimeProvider;
    @Mock private ResponseFactory responseFactory;
    @Mock private MailGateway mailGateway;
    @Mock private UpdateLog updateLog;
    @Mock private LoggerContext loggerContext;

    private PendingUpdatesCleanup subject;

    @Before
    public void setup() {
        subject = new PendingUpdatesCleanup(pendingUpdateDao, rpslObjectDao, dateTimeProvider, responseFactory, mailGateway, updateLog, loggerContext);
    }

    @Test
    public void cleanup_pending_update() {
        final LocalDateTime now = LocalDateTime.now();
        final PendingUpdate pendingUpdate = new PendingUpdate(Sets.<String>newHashSet("AUTH"), RpslObject.parse("route: 10.0.0.0/8\norigin: AS123\nmnt-by: OWNER-MNT\nsource: TEST"), now);
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(now);
        when(pendingUpdateDao.findBeforeDate(any(LocalDateTime.class))).thenReturn(Lists.<PendingUpdate>newArrayList(pendingUpdate));
        when(rpslObjectDao.getByKeys(any(ObjectType.class), any(List.class))).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: OWNER-MNT\nupd-to: test@ripe.net")));

        subject.run();

        verify(pendingUpdateDao).findBeforeDate(now.minusDays(7));
        verify(pendingUpdateDao).remove(pendingUpdate);
        verify(updateLog).logUpdateResult(any(UpdateRequest.class), any(UpdateContext.class), any(Update.class), any(Stopwatch.class));
        verify(mailGateway).sendEmail(any(String.class), any(ResponseMessage.class));
    }
}
