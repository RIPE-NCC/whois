package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RouteAutnumAuthenticationTest {
    @Mock
    PreparedUpdate update;
    @Mock
    UpdateContext updateContext;
    @Mock
    AuthenticationModule authenticationModule;
    @Mock
    RpslObjectDao objectDao;
    @InjectMocks
    RouteAutnumAuthentication subject;
    private RpslObject routeObject;

    @Before
    public void setUp() throws Exception {
        routeObject = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS513\n" +
                "mnt-routes: ROUTE-MNT");

        when(update.getUpdatedObject()).thenReturn(routeObject);
    }

    @Test
    public void supports_route_action_create() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getAction()).thenReturn(Action.CREATE);
        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void supports_route6_action_create() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getAction()).thenReturn(Action.CREATE);

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void no_autnum_found_for_origin_value() {
        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenThrow(EmptyResultDataAccessException.class);

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(update.getUpdatedObject(), AttributeType.ORIGIN, Collections.<RpslObject>emptyList())));
        }
    }

    @Test
    public void no_maintainers_found_in_autnum() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(autNum, AttributeType.MNT_BY, Collections.<RpslObject>emptyList())));
        }
    }

    @Test
    public void autnum_with_mntroutes_not_exists() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-routes: ROUTE-MNT\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final List<RpslObject> candidates = Collections.emptyList();
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(autNum, AttributeType.MNT_ROUTES, candidates)));
        }
    }

    @Test
    public void autnum_with_mntroutes_auth_fails() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n" +
                "mnt-lower: ROUTE-LOWER\n" +
                "mnt-routes: ROUTE-MNT\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final List<RpslObject> candidates = Lists.newArrayList(RpslObject.parse("mntner: ROUTE-MNT"));
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(autNum, AttributeType.MNT_ROUTES, candidates)));
        }
    }

    @Test
    public void autnum_with_mntroutes_auth_succeeds() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n" +
                "mnt-lower: ROUTE-LOWER\n" +
                "mnt-routes: ROUTE-MNT\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final RpslObject maintainer = RpslObject.parse("mntner: ROUTE-MNT");

        final List<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated, contains(maintainer));

        verify(objectDao, never()).getByKeys(ObjectType.MNTNER, ciSet("ROUTE-LOWER"));
        verify(objectDao, never()).getByKeys(ObjectType.MNTNER, ciSet("ROUTE-BY"));
    }

    @Test
    public void autnum_with_mntlower_auth_skipped() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n" +
                "mnt-lower: ROUTE-LOWER\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final List<RpslObject> candidates = Lists.newArrayList(RpslObject.parse("mntner: ROUTE-BY"));
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-BY"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(autNum, AttributeType.MNT_BY, candidates)));
        }
    }

    @Test
    public void autnum_with_mntlower_auth_succeeds() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n" +
                "mnt-lower: ROUTE-LOWER\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final RpslObject maintainer = RpslObject.parse("mntner: ROUTE-BY");

        final List<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-BY"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated, contains(maintainer));
    }

    @Test
    public void autnum_with_mntby_auth_fails() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final List<RpslObject> candidates = Lists.newArrayList(RpslObject.parse("mntner: ROUTE-BY"));
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-BY"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(autNum, AttributeType.MNT_BY, candidates)));
        }
    }

    @Test
    public void autnum_with_mntby_auth_succeeds() {
        final RpslObject autNum = RpslObject.parse("" +
                "aut-num: AS513\n" +
                "as-name: AS-5354\n" +
                "mnt-by: ROUTE-BY\n");

        when(objectDao.getByKey(ObjectType.AUT_NUM, "AS513")).thenReturn(autNum);
        final RpslObject maintainer = RpslObject.parse("mntner: ROUTE-BY");

        final List<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTE-BY"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated, contains(maintainer));
    }
}
