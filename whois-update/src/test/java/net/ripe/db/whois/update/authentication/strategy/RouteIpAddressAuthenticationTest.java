package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.iptree.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RouteIpAddressAuthenticationTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Ipv4RouteTree routeTree;
    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6RouteTree route6Tree;
    @Mock Ipv6Tree ipv6Tree;
    @Mock AuthenticationModule authenticationModule;
    @Mock RpslObjectDao objectDao;
    @InjectMocks RouteIpAddressAuthentication subject;

    RpslObject routeObject;
    Ipv4Resource routeResource;

    @Before
    public void setUp() throws Exception {
        routeObject = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS513\n" +
                "mnt-routes: ROUTE-MNT ANY");

        routeResource = Ipv4Resource.parse(routeObject.getTypeAttribute().getCleanValue());

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
    public void exact_match_route_without_mntRoutes_succeeds() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS12\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());

        final Ipv4RouteEntry existingRouteEntry = new Ipv4RouteEntry(existingRouteResource, 1, "AS12");
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("TEST-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticatedMaintainers = subject.authenticate(update, updateContext);
        assertThat(authenticatedMaintainers, contains(maintainer));
    }

    @Test
    public void exact_match_route_without_mntRoutes_fails() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS12\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        final Ipv4RouteEntry existingRouteEntry = new Ipv4RouteEntry(existingRouteResource, 1, "AS12");

        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("TEST-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(existingRoute, AttributeType.MNT_BY, candidates)));
        }
    }

    @Test
    public void exact_match_route_with_mntRoutes_succeeds() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS12\n" +
                "mnt-routes: ROUTES-MNT\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        final Ipv4RouteEntry existingRouteEntry = new Ipv4RouteEntry(existingRouteResource, 1, "AS12");
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: ROUTES-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTES-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticatedMaintainers = subject.authenticate(update, updateContext);
        assertThat(authenticatedMaintainers, contains(maintainer));

        verify(objectDao, never()).getByKeys(ObjectType.MNTNER, ciSet("TEST-MNT"));
    }

    @Test
    public void exact_match_route_with_mntRoutes_fails() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91.244.0/23\n" +
                "origin: AS12\n" +
                "mnt-routes: ROUTES-MNT\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        final Ipv4RouteEntry existingRouteEntry = new Ipv4RouteEntry(existingRouteResource, 1, "AS12");
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: ROUTES-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("ROUTES-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(existingRoute, AttributeType.MNT_ROUTES, candidates)));
        }
    }

    @Test
    public void less_specific_match_route_with_mntRoutes_fails() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91/16\n" +
                "origin: AS12\n" +
                "mnt-lower: LOWER-MNT\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        final Ipv4RouteEntry existingRouteEntry = new Ipv4RouteEntry(existingRouteResource, 1, "AS12");
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: LOWER-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("LOWER-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(existingRoute, AttributeType.MNT_LOWER, candidates)));
        }
    }

    @Test
    public void less_specific_match_inetnum_with_mntRoutes_fails() {
        final RpslObject existingRoute = RpslObject.parse("" +
                "route: 192.91/16\n" +
                "origin: AS12\n" +
                "mnt-lower: LOWER-MNT\n" +
                "mnt-by: TEST-MNT");

        final Ipv4Resource existingRouteResource = Ipv4Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Collections.<Ipv4RouteEntry>emptyList());
        when(ipv4Tree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Lists.newArrayList(new Ipv4Entry(existingRouteResource, 1)));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: LOWER-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("LOWER-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(existingRoute, AttributeType.MNT_LOWER, candidates)));
        }
    }

    @Test
    public void no_match_ipObject() {
        when(routeTree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Collections.<Ipv4RouteEntry>emptyList());
        when(ipv4Tree.findExactOrFirstLessSpecific(routeResource)).thenReturn(Collections.<Ipv4Entry>emptyList());
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyListOf(RpslObject.class))).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(routeObject, AttributeType.ROUTE, Collections.<RpslObject>emptyList())));
        }
    }

    @Test
    public void less_specific_match_route6_without_mntRoutes_fails() {
        final RpslObject route6Object = RpslObject.parse("" +
                "route6: acac::0/32\n" +
                "origin: AS12\n" +
                "mnt-routes: ROUTE-MNT");

        final Ipv6Resource route6Resource = Ipv6Resource.parse(route6Object.getTypeAttribute().getCleanValue());
        when(update.getUpdatedObject()).thenReturn(route6Object);


        final RpslObject existingRoute = RpslObject.parse("" +
                "route6: acac::0/16\n" +
                "origin: AS12\n" +
                "mnt-lower: LOWER-MNT\n" +
                "mnt-by: TEST-MNT");

        final Ipv6Resource existingRouteResource = Ipv6Resource.parse(existingRoute.getTypeAttribute().getCleanValue());
        final Ipv6RouteEntry existingRouteEntry = new Ipv6RouteEntry(existingRouteResource, 1, "AS12");
        when(route6Tree.findExactOrFirstLessSpecific(route6Resource)).thenReturn(Lists.newArrayList(existingRouteEntry));
        when(objectDao.getById(1)).thenReturn(existingRoute);

        final RpslObject maintainer = RpslObject.parse("mntner: LOWER-MNT\n");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("LOWER-MNT"))).thenReturn(candidates);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(Collections.<RpslObject>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected authentication exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(existingRoute, AttributeType.MNT_LOWER, candidates)));
        }
    }

}
