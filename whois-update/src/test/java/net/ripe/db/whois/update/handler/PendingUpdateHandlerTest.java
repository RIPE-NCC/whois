package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PendingUpdateHandlerTest {
    @Mock private PendingUpdateDao pendingUpdateDao;
    @Mock private PreparedUpdate preparedUpdate;
    @Mock private UpdateContext updateContext;
    @Mock private Authenticator authenticator;
    @Mock private LoggerContext loggerContext;
    @Mock private Subject subject;
    @Mock private DateTimeProvider dateTimeProvider;

    @InjectMocks private PendingUpdateHandler testSubject;

    @Before
    public void setup() {
        when(dateTimeProvider.getCurrentDate()).thenReturn(LocalDate.now());
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void found_completing_pendingUpdate() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        RpslObject objectBase = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        final PendingUpdate pendingUpdate = new PendingUpdate(Sets.newHashSet("RouteAutnumAuthentication"), objectBase, dateTimeProvider.getCurrentDateTime());

        when(updateContext.getPendingUpdate(preparedUpdate)).thenReturn(pendingUpdate);
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet("RouteIpAddressAuthentication"));

        testSubject.handle(preparedUpdate, updateContext);

        verify(pendingUpdateDao, never()).store(pendingUpdate);
    }

    @Test
    public void found_pendingUpdate_with_same_authenticator() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        RpslObject objectBase = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        final PendingUpdate pendingUpdate = new PendingUpdate(Sets.newHashSet("RouteAutnumAuthentication"), objectBase, dateTimeProvider.getCurrentDateTime());

        when(updateContext.getPendingUpdate(preparedUpdate)).thenReturn(pendingUpdate);
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet("RouteAutnumAuthentication"));

        testSubject.handle(preparedUpdate, updateContext);

        verify(pendingUpdateDao, never()).store(any(PendingUpdate.class));
        verify(updateContext).setAction(any(UpdateContainer.class), eq(Action.NOOP));
    }

    @Test
    public void did_not_find_pendingUpdate() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");

        when(updateContext.getPendingUpdate(preparedUpdate)).thenReturn(null);
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet("RouteAutnumAuthentication"));

        testSubject.handle(preparedUpdate, updateContext);

        verify(pendingUpdateDao).store(any(PendingUpdate.class));
    }
}
