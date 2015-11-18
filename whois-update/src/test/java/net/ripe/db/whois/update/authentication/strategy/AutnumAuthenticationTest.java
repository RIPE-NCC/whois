package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutnumAuthenticationTest {

    @Mock private RpslObjectDao objectDao;
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private AuthenticationModule authenticationModule;

    @InjectMocks AutnumAuthentication subject;

    @Test
    public void supports_autnum_create() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);

        final boolean supported = subject.supports(update);

        assertThat(supported, is(true));
    }

    @Test
    public void supports_other_than_autnum_create() {
        for (final ObjectType objectType : ObjectType.values()) {
            if (ObjectType.AUT_NUM.equals(objectType)) {
                continue;
            }

            when(update.getAction()).thenReturn(Action.CREATE);
            when(update.getType()).thenReturn(ObjectType.AS_BLOCK);
            final boolean supported = subject.supports(update);
            assertThat(supported, is(false));

            reset(update);
        }
    }

    @Test
    public void supports_autnum_modify() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);

        final boolean supported = subject.supports(update);

        assertThat(supported, is(false));
    }

    @Test
    public void supports_autnum_delete() {
        when(update.getAction()).thenReturn(Action.DELETE);
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);

        final boolean supported = subject.supports(update);

        assertThat(supported, is(false));
    }

    @Test
    public void authenticated_by_mntlower_succeeds() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS3333"));
        when(objectDao.findAsBlock(3333, 3333)).thenReturn(RpslObject.parse("as-block: AS3209 - AS3353\nmnt-lower: LOW-MNT"));

        final ArrayList<RpslObject> parentMaintainers = Lists.newArrayList(RpslObject.parse("mntner: LOW-MNT"));
        when(objectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(parentMaintainers);
        when(authenticationModule.authenticate(update, updateContext, parentMaintainers)).thenReturn(parentMaintainers);

        final List<RpslObject> authenticatedBy = subject.authenticate(update, updateContext);

        assertThat(authenticatedBy.equals(parentMaintainers), is(true));
        verifyZeroInteractions(updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void authenticated_by_mntlower_fails() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS3333"));
        when(objectDao.findAsBlock(3333, 3333)).thenReturn(RpslObject.parse("as-block: AS3209 - AS3353\nmnt-by: TEST-MNT\nmnt-lower: LOW-MNT"));

        final ArrayList<RpslObject> parentMaintainers = Lists.newArrayList(RpslObject.parse("mntner: LOW-MNT"));
        when(objectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(parentMaintainers);
        when(authenticationModule.authenticate(update, updateContext, parentMaintainers)).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void cant_find_asblock() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS3333"));
        when(objectDao.findAsBlock(3333, 3333)).thenReturn(null);

        subject.authenticate(update, updateContext);
    }

    @Test
    public void authenticated_by_mntby_succeeds() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS3333"));
        when(objectDao.findAsBlock(3333, 3333)).thenReturn(RpslObject.parse("as-block: AS3209 - AS3353\nmnt-by: TEST-MNT"));

        final ArrayList<RpslObject> parentMaintainers = Lists.newArrayList(RpslObject.parse("mntner: TEST-MNT"));
        when(objectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(parentMaintainers);
        when(authenticationModule.authenticate(update, updateContext, parentMaintainers)).thenReturn(parentMaintainers);

        final List<RpslObject> authenticatedBy = subject.authenticate(update, updateContext);

        assertThat(authenticatedBy.equals(parentMaintainers), is(true));
        verifyZeroInteractions(updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void authenticated_by_mntby_fails() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS3333"));
        when(objectDao.findAsBlock(3333, 3333)).thenReturn(RpslObject.parse("as-block: AS3209 - AS3353\nmnt-by: TEST-MNT"));

        final ArrayList<RpslObject> parentMaintainers = Lists.newArrayList(RpslObject.parse("mntner: TEST-MNT"));
        when(objectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(parentMaintainers);
        when(authenticationModule.authenticate(update, updateContext, parentMaintainers)).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);
    }

    @Test
    public void max_value() {
        final long maxValue = (1L << 32) - 1;
        final RpslObject autNum = RpslObject.parse("aut-num: AS" + maxValue);
        final RpslObject asBlock = RpslObject.parse("as-block: AS" + (maxValue - 1) + " - AS" + maxValue + "\nmnt-by: TEST-MNT");

        when(update.getUpdatedObject()).thenReturn(autNum);
        when(objectDao.findAsBlock(maxValue, maxValue)).thenReturn(asBlock);

        final ArrayList<RpslObject> parentMaintainers = Lists.newArrayList(RpslObject.parse("mntner: TEST-MNT"));
        when(objectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(parentMaintainers);
        when(authenticationModule.authenticate(update, updateContext, parentMaintainers)).thenReturn(parentMaintainers);

        subject.authenticate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
