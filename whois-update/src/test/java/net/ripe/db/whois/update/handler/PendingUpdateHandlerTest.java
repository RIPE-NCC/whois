package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.authentication.strategy.RouteAutnumAuthentication;
import net.ripe.db.whois.update.authentication.strategy.RouteIpAddressAuthentication;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.PendingUpdate;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PendingUpdateHandlerTest {
    @Mock private PendingUpdateDao pendingUpdateDao;
    @Mock private PreparedUpdate preparedUpdate;
    @Mock private UpdateContext updateContext;
    @Mock private Authenticator authenticator;
    @Mock private Subject subject;

    @InjectMocks private PendingUpdateHandler testSubject;

    @Test
    public void found_completing_pendingUpdate() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        RpslObject objectBase = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        final PendingUpdate pendingUpdate = new PendingUpdate(Sets.newHashSet(RouteAutnumAuthentication.class.toString()), objectBase);

        when(pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.newArrayList(pendingUpdate));
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet(RouteIpAddressAuthentication.class.toString()));

        testSubject.handle(preparedUpdate, updateContext);

        Mockito.verify(pendingUpdateDao, never()).store(pendingUpdate);
    }

    @Test
    public void found_pendingUpdate_with_same_authenticator() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        RpslObject objectBase = RpslObject.parse("route: 193.0/16\norigin: AS12345");
        final PendingUpdate pendingUpdate = new PendingUpdate(Sets.newHashSet(RouteAutnumAuthentication.class.toString()), objectBase);

        when(pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.newArrayList(pendingUpdate));
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet(RouteAutnumAuthentication.class.toString()));

        testSubject.handle(preparedUpdate, updateContext);

        Mockito.verify(pendingUpdateDao, times(1)).store(any(PendingUpdate.class));
    }

    @Test
    public void did_not_find_pendingUpdate() {
        RpslObject object = RpslObject.parse("route: 193.0/16\norigin: AS12345");

        when(pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.<PendingUpdate>newArrayList());
        when(preparedUpdate.getUpdatedObject()).thenReturn(object);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.getPassedAuthentications()).thenReturn(Sets.newHashSet(RouteAutnumAuthentication.class.toString()));

        testSubject.handle(preparedUpdate, updateContext);

        Mockito.verify(pendingUpdateDao, times(1)).store(any(PendingUpdate.class));
    }
}
