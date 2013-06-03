package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RelatedIrtDecoratorTest {
    @Mock
    private HierarchyLookupIpv4 hierarchyLookupIpv4;
    @Mock
    private HierarchyLookupIpv6 hierarchyLookupIpv6;
    @InjectMocks
    private RelatedIrtDecorator subject;

    @Test
    public void appliesToQuery_empty() {
        assertThat(subject.appliesToQuery(Query.parse("foo")), is(false));
    }

    @Test
    public void appliesToQuery_no_irt() {
        assertThat(subject.appliesToQuery(Query.parse("-T inetnum 10.0.0.0")), is(false));
    }

    @Test
    public void appliesToQuery_capital_C() {
        assertThat(subject.appliesToQuery(Query.parse("-C -T inetnum 10.0.0.0")), is(false));
    }

    @Test
    public void appliesToQuery_irt() {
        assertThat(subject.appliesToQuery(Query.parse("-c -T inetnum 10.0.0.0")), is(true));
    }

    @Test
    public void decorate_not_supported() {
        final RpslObject rpslObject = RpslObject.parse("poem:RIPE");
        final Collection<RpslObjectInfo> infos = subject.decorate(Query.parse("RIPE"), rpslObject);

        verify(hierarchyLookupIpv4, times(1)).supports(rpslObject);
        verify(hierarchyLookupIpv6, times(1)).supports(rpslObject);
        assertThat(infos, hasSize(0));
    }

    @Test
    public void decorate_inetnum() {
        final RpslObject rpslObject = RpslObject.parse("inetnum:0.0.0.0");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(0, ObjectType.IRT, "IRT");
        final List<RpslObjectInfo> result = Arrays.asList(rpslObjectInfo);

        when(hierarchyLookupIpv4.supports(rpslObject)).thenReturn(true);
        when(hierarchyLookupIpv4.getReferencedIrtsInHierarchy(rpslObject)).thenReturn(result);

        final Collection<RpslObjectInfo> infos = subject.decorate(Query.parse("0.0.0.0"), rpslObject);

        verify(hierarchyLookupIpv4, times(1)).supports(rpslObject);
        assertThat(infos, contains(rpslObjectInfo));
    }

    @Test
    public void decorate_inet6num() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: ::0");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(0, ObjectType.IRT, "IRT");
        final List<RpslObjectInfo> result = Arrays.asList(rpslObjectInfo);

        when(hierarchyLookupIpv6.supports(rpslObject)).thenReturn(true);
        when(hierarchyLookupIpv6.getReferencedIrtsInHierarchy(rpslObject)).thenReturn(result);

        final Collection<RpslObjectInfo> infos = subject.decorate(Query.parse("::0"), rpslObject);

        verify(hierarchyLookupIpv6, times(1)).supports(rpslObject);
        assertThat(infos, contains(rpslObjectInfo));
    }
}
