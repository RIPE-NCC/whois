package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InetnumAuthenticationTest {
    @Mock private AuthenticationModule authenticationModule;
    @Mock private Ipv4Tree ipv4Tree;
    @Mock private Ipv6Tree ipv6Tree;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private Ipv4Entry ipv4Entry;
    @Mock private Ipv6Entry ipv6Entry;

    @InjectMocks private InetnumAuthentication subject;

    @Test
    public void supports_creating_inetnum() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.INETNUM);

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void supports_creating_inet6num() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.INET6NUM);

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void does_not_support_modifying() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.INETNUM);

        assertThat(subject.supports(update), is(false));
    }


    @Test
    public void authenticate_mntlower_inetnum_succeeds() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24"));

        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nmnt-lower: LWR-MNT");
        when(rpslObjectDao.getById(anyInt())).thenReturn(parent);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(ipv4Entry));

        final RpslObject lowerMaintainer = RpslObject.parse("mntner: LWR-MNT");

        final ArrayList<RpslObject> lowerMaintainers = Lists.newArrayList(lowerMaintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, parent.getValuesForAttribute(AttributeType.MNT_LOWER))).thenReturn(lowerMaintainers);

        when(authenticationModule.authenticate(update, updateContext, lowerMaintainers)).thenReturn(lowerMaintainers);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(lowerMaintainer));
        verifyZeroInteractions(updateContext);
    }

    @Test
    public void authenticate_mntby_inetnum_succeeds() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24"));

        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nmnt-by: TEST-MNT");
        when(rpslObjectDao.getById(anyInt())).thenReturn(parent);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(ipv4Entry));

        final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT");

        final ArrayList<RpslObject> maintainers = Lists.newArrayList(maintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, parent.getValuesForAttribute(AttributeType.MNT_BY))).thenReturn(maintainers);

        when(authenticationModule.authenticate(update, updateContext, maintainers)).thenReturn(maintainers);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(maintainer));
        verifyZeroInteractions(updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void authenticate_mntlower_inetnum_fails() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24"));

        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nmnt-lower: LWR-MNT");
        when(rpslObjectDao.getById(anyInt())).thenReturn(parent);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(ipv4Entry));

        final RpslObject maintainer = RpslObject.parse("mntner: LWR-MNT");

        final ArrayList<RpslObject> maintainers = Lists.newArrayList(maintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, parent.getValuesForAttribute(AttributeType.MNT_LOWER))).thenReturn(maintainers);

        when(authenticationModule.authenticate(update, updateContext, maintainers)).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);
    }

    @Test
    public void authenticate_mntlower_inet6num_succeeds() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: fe80::/32"));

        final RpslObject parent = RpslObject.parse("inetnum: fe80::/16\nmnt-lower: LWR-MNT");
        when(rpslObjectDao.getById(anyInt())).thenReturn(parent);
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(ipv6Entry));

        final RpslObject maintainer = RpslObject.parse("mntner: LWR-MNT");

        final ArrayList<RpslObject> maintainers = Lists.newArrayList(maintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, parent.getValuesForAttribute(AttributeType.MNT_LOWER))).thenReturn(maintainers);

        when(authenticationModule.authenticate(update, updateContext, maintainers)).thenReturn(Lists.<RpslObject>newArrayList(maintainer));

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(maintainer));
        verifyZeroInteractions(updateContext);
    }

    @Test
    public void parent_not_found() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: fe80::/32"));
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.<Ipv6Entry>newArrayList());

        List<RpslObject> rpslObjects = subject.authenticate(update, updateContext);

        assertThat(rpslObjects, is(empty()));
    }

    @Test
    public void more_than_one_parent_found() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: fe80::/32"));
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.<Ipv6Entry>newArrayList(ipv6Entry, ipv6Entry));

        List<RpslObject> rpslObjects = subject.authenticate(update, updateContext);

        assertThat(rpslObjects, is(empty()));
    }

}
