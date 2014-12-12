package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.mail.MailGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateNotifierTest {
    @Mock UpdateRequest updateRequest;
    @Mock UpdateContext updateContext;

    @Mock RpslObjectDao rpslObjectDao;
    @Mock ResponseFactory responseFactory;
    @Mock MailGateway mailGateway;
    @Mock ResponseMessage responseMessage;

    @InjectMocks UpdateNotifier subject;

    @Test
    public void sendNotifications_empty() {
        when(updateRequest.getUpdates()).thenReturn(Lists.<Update>newArrayList());

        subject.sendNotifications(updateRequest, updateContext);

        verifyZeroInteractions(responseFactory, mailGateway);
    }

    @Test
    public void sendNotifications_noPreparedUpdate() {
        final Update update = mock(Update.class);
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));

        subject.sendNotifications(updateRequest, updateContext);

        verifyZeroInteractions(responseFactory, mailGateway);
    }

    @Test
    public void sendNotifications_single_no_notifications() {
        final Update update = mock(Update.class);
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "mnt-nfy: mntnotify1@me.com\n" +
                "mnt-nfy: mntnotify2@me.com\n" +
                "notify: notify1@me.com\n" +
                "notify: notify2@me.com\n" +
                "referral-by: ADMIN-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source: TEST\n");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, rpslObject, Action.CREATE);

        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));
        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(updateContext.getStatus(preparedUpdate)).thenReturn(UpdateStatus.SUCCESS);

        subject.sendNotifications(updateRequest, updateContext);

        verify(mailGateway).sendEmail(eq("notify1@me.com"), any(ResponseMessage.class));
        verify(mailGateway).sendEmail(eq("notify2@me.com"), any(ResponseMessage.class));
    }

    @Test
    public void sendNotifications_sanitized_email() {
        final Update update = mock(Update.class);
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "mnt-nfy: mntnotify1@me.com\n" +
                "mnt-nfy: mntnotify2@me.com\n" +
                "notify: notifies us <mailto:notify@me.com>\n" +
                "referral-by: ADMIN-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source: TEST\n");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, rpslObject, Action.CREATE);

        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));
        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(updateContext.getStatus(preparedUpdate)).thenReturn(UpdateStatus.SUCCESS);
        when(responseFactory.createNotification(eq(updateContext), any(Origin.class), any(Notification.class))).thenReturn(responseMessage);

        subject.sendNotifications(updateRequest, updateContext);

        verify(mailGateway, never()).sendEmail("notifies us <mailto:notify@me.com>", responseMessage);
    }
}
