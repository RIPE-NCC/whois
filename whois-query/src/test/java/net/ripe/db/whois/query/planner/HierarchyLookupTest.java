package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HierarchyLookupTest {
    @Mock Ipv4Tree ipv4Tree;
    @Mock RpslObjectDao rpslObjectDao;
    @InjectMocks HierarchyLookupIpv4 subject;

    @Test
    public void getAttributeKeysInHierarchyMostSpecific_noMatch() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 10.0.0.0");

        when(ipv4Tree.findAllLessSpecific(any(Ipv4Resource.class))).thenReturn(Collections.<Ipv4Entry>emptyList());

        final Collection<RpslObjectInfo> result = subject.getReferencedIrtsInHierarchy(rpslObject);
        assertThat(result, hasSize(0));
    }

    @Test
    public void getAttributeKeysInHierarchyMostSpecific_match_in_object() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 10.0.0.0\nmnt-irt: IRT");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.IRT, "IRT");
        when(rpslObjectDao.findByKey(ObjectType.IRT, "IRT")).thenReturn(rpslObjectInfo);

        final Collection<RpslObjectInfo> result = subject.getReferencedIrtsInHierarchy(rpslObject);
        assertThat(result, contains(rpslObjectInfo));

        verifyZeroInteractions(ipv4Tree);
    }

    @Test
    public void getAttributeKeysInHierarchyMostSpecific_match_in_hierarchy() {
        final Ipv4Entry ipv4Entry2 = new Ipv4Entry(Ipv4Resource.parse("193.0.0/24"), 2);
        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, ObjectType.IRT, "IRT");

        final Ipv4Entry ipv4Entry3 = new Ipv4Entry(Ipv4Resource.parse("193.0/16"), 3);

        when(ipv4Tree.findAllLessSpecific(Ipv4Resource.parse("193.0.0.10"))).thenReturn(Lists.newArrayList(ipv4Entry2, ipv4Entry3));
        when(rpslObjectDao.getById(ipv4Entry2.getObjectId())).thenReturn(RpslObject.parse("inetnum: 193.0.0.0-193.0.0.255\nmnt-irt: IRT"));
        when(rpslObjectDao.findByKey(ObjectType.IRT, "IRT")).thenReturn(rpslObjectInfo);

        final Collection<RpslObjectInfo> result = subject.getReferencedIrtsInHierarchy(RpslObject.parse("inetnum: 193.0.0.10"));

        assertThat(result, contains(rpslObjectInfo));
        verify(rpslObjectDao, never()).getById(ipv4Entry3.getObjectId());
    }

    @Test
    public void getAttributeKeysInHierarchyMostSpecific_none_in_hierarchy() {
        final Ipv4Entry ipv4Entry2 = new Ipv4Entry(Ipv4Resource.parse("193.0.0/24"), 2);
        final Ipv4Entry ipv4Entry3 = new Ipv4Entry(Ipv4Resource.parse("193.0/16"), 3);

        when(ipv4Tree.findAllLessSpecific(Ipv4Resource.parse("193.0.0.10"))).thenReturn(Lists.newArrayList(ipv4Entry2, ipv4Entry3));
        when(rpslObjectDao.getById(ipv4Entry2.getObjectId())).thenReturn(RpslObject.parse("inetnum: 193.0.0.0-193.0.0.255"));
        when(rpslObjectDao.getById(ipv4Entry3.getObjectId())).thenReturn(RpslObject.parse("inetnum: 193.0.0.0-193.0.255.255"));

        final Collection<RpslObjectInfo> result = subject.getReferencedIrtsInHierarchy(RpslObject.parse("inetnum: 193.0.0.10"));

        assertThat(result, hasSize(0));
    }
}
