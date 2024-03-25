package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Notification;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.mail.WhoisMailGatewaySmtp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateNotifierTest {
    @Mock UpdateRequest updateRequest;
    @Mock UpdateContext updateContext;
    @Mock Origin origin;

    @Mock RpslObjectDao rpslObjectDao;
    @Mock ResponseFactory responseFactory;
    @Mock WhoisMailGatewaySmtp mailGateway;
    @Mock ResponseMessage responseMessage;

    @InjectMocks UpdateNotifier subject;

    @Test
    public void sendNotifications_empty() {
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList());

        subject.sendNotifications(updateRequest, updateContext);

        verifyNoMoreInteractions(responseFactory, mailGateway);
    }

    @Test
    public void sendNotifications_noPreparedUpdate() {
        final Update update = mock(Update.class);
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));

        subject.sendNotifications(updateRequest, updateContext);

        verifyNoMoreInteractions(responseFactory, mailGateway);
    }

    @Test
    public void sendNotifications_single_no_notifications() {
        final Update update = mock(Update.class);
        final RpslObject rpslObject = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "mnt-nfy: mntnotify1@me.com\n" +
                "mnt-nfy: mntnotify2@me.com\n" +
                "notify: notify1@me.com\n" +
                "notify: notify2@me.com\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, rpslObject, Action.CREATE);

        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));
        when(updateRequest.getOrigin()).thenReturn(origin);
        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(updateContext.getStatus(preparedUpdate)).thenReturn(UpdateStatus.SUCCESS);
        ResponseMessage responseMessage = new ResponseMessage("Notification of RIPE Database changes", "message");
        when(responseFactory.createNotification(any(UpdateContext.class), any(Origin.class), any(Notification.class))).thenReturn(responseMessage);

        subject.sendNotifications(updateRequest, updateContext);

        verify(mailGateway).sendEmail(eq("notify1@me.com"), eq(responseMessage));
        verify(mailGateway).sendEmail(eq("notify2@me.com"), eq(responseMessage));
    }

    @Test
    public void sendNotifications_sanitized_email() {
        final Update update = mock(Update.class);
        final RpslObject rpslObject = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "mnt-nfy: mntnotify1@me.com\n" +
                "mnt-nfy: mntnotify2@me.com\n" +
                "notify: notifies us <mailto:notify@me.com>\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, rpslObject, Action.CREATE);

        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));
        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(updateContext.getStatus(preparedUpdate)).thenReturn(UpdateStatus.SUCCESS);

        subject.sendNotifications(updateRequest, updateContext);

        verify(mailGateway, never()).sendEmail("notifies us <mailto:notify@me.com>", responseMessage);
    }
}
