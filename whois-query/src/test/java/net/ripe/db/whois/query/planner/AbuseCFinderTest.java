package net.ripe.db.whois.query.planner;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbuseCFinderTest {

    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6Tree ipv6Tree;
    @Mock RpslObjectDao objectDao;
    @Mock Maintainers maintainers;

    @InjectMocks AbuseCFinder subject;

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(ciSet("RS1-MNT", "RS2-MNT"));
    }

    @Test
    public void getAbuseContacts_oneOrgReferenceNoAbuseC() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1");
        final RpslObject parent = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\nmnt-by: RS2-MNT");
        when(objectDao.getByKeys(ObjectType.ORGANISATION, ciSet("ORG-TEST1"))).thenReturn(Lists.<RpslObject>newArrayList());
        when(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(inetnum.getKey()))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(parent.getKey()), 5)));
        when(objectDao.getById(5)).thenReturn(parent);

        final Map<CIString, CIString> abuseContacts = subject.getAbuseContacts(inetnum);

        assertTrue(abuseContacts.isEmpty());
    }

    @Test
    public void getAbuseContacts_oneOrgReferenceOneAbuseC() {
        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT");
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1");

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(inetnum.getKey());
        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(root.getKey()), 1)));
        when(objectDao.getByKeys(ObjectType.ORGANISATION, ciSet("ORG-TEST1"))).thenReturn(Lists.newArrayList(RpslObject.parse("organisation: ORG-TEST1\nabuse-c: ABU-TEST")));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: abuse role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: ABU-TEST")));

        final Map<CIString, CIString> abuseContacts = subject.getAbuseContacts(inetnum);

        assertThat(abuseContacts.size(), is(1));
        final CIString abuseContact = abuseContacts.get(inetnum.getKey());
        assertThat(abuseContact.toString(), containsString("abuse@ripe.net"));
    }

    @Test
    public void getAbuseContacts_inetnum() {
        final RpslObject object = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG1-TEST");
        final RpslObject org = RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST");
        final RpslObject role = RpslObject.parse("role: A Role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: AB-TEST");
        when(objectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(Lists.newArrayList(org));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(role));

        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(object.getKey());
        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(root.getKey()), 1)));

        final Map<CIString, CIString> abuseContacts = subject.getAbuseContacts(object);

        assertThat(abuseContacts.get(object.getKey()).toString(), containsString("abuse@ripe.net"));
    }

    @Test
    public void getAbuseContacts_inetnum_parent() {
        final RpslObject child = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255");
        final RpslObject parent = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG1-TEST");
        final RpslObject org = RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST");
        final RpslObject role = RpslObject.parse("role: A Role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: AB-TEST");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(parent.getKey());
        final Ipv4Entry ipv4Entry = new Ipv4Entry(ipv4Resource, 1);

        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(ipv4Entry));
        when(objectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(Lists.newArrayList(org));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(role));

        final Map<CIString, CIString> abuseContacts = subject.getAbuseContacts(child);

        assertThat(abuseContacts.get(child.getKey()).toString(), containsString("abuse@ripe.net"));
    }

    @Test
    public void getAbuseContacts_rsMaintained() {
        final RpslObject object = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\nmnt-lower: RS2-MNT");

        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(object.getKey());
        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(root.getKey()), 1)));
        when(objectDao.getById(1)).thenReturn(root);

        final Map<CIString, CIString> abuseContacts = subject.getAbuseContacts(object);

        assertThat(abuseContacts.size(), is(0));
    }

    @Test
    public void getAbuseContacs_autnum_with_abuseccontacts() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS8462\norg: ORG1-TEST");
        final RpslObject organisation = RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST");
        final RpslObject role = RpslObject.parse("role: Abuse Role\nnic-hdl: AB-TEST\nabuse-mailbox: abuse@ripe.net");
        when(objectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(Lists.newArrayList(organisation));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anySet())).thenReturn(Lists.newArrayList(role));

        final Map<CIString, CIString> result = subject.getAbuseContacts(autnum);

        assertThat(result.get(autnum.getKey()).toString(), containsString("abuse@ripe.net"));
        verifyZeroInteractions(ipv4Tree);
        verifyZeroInteractions(ipv6Tree);
    }

    @Test
    public void getAbuseContacts_autnum_without_abusecontacts() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS8462\norg: ORG1-TEST");
        final RpslObject organisation = RpslObject.parse("organisation: ORG1-TEST");

        when(objectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(Lists.newArrayList(organisation));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anySet())).thenReturn(Lists.<RpslObject>newArrayList());

        final Map<CIString, CIString> result = subject.getAbuseContacts(autnum);

        assertThat(result.size(), is(0));
    }

    @Test
    public void getAbuseContacts_rootObject() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0");

        final Map<CIString, CIString> result = subject.getAbuseContacts(inetnum);
        assertThat(result.size(), is(0));
    }
}
