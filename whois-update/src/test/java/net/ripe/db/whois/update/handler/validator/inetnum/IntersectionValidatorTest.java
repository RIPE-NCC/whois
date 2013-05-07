package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IntersectionValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6Tree ipv6Tree;
    @InjectMocks IntersectionValidator subject;

    RpslObject parentIpv4;
    Ipv4Resource parentIpv4Key;
    Ipv4Entry parentIpv4Entry;

    RpslObject parentIpv6;
    Ipv6Resource parentIpv6Key;
    Ipv6Entry parentIpv6Entry;

    @Before
    public void setUp() throws Exception {
        parentIpv4 = RpslObject.parse("inetnum: 0/0");
        parentIpv4Key = Ipv4Resource.parse(parentIpv4.getKey());
        parentIpv4Entry = new Ipv4Entry(parentIpv4Key, 1);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(parentIpv4Entry));

        parentIpv6 = RpslObject.parse("inet6num: ::0/0");
        parentIpv6Key = Ipv6Resource.parse(parentIpv6.getKey());
        parentIpv6Entry = new Ipv6Entry(parentIpv6Key, 2);
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(parentIpv6Entry));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void validate_no_children_ipv4() {
        final RpslObject object = RpslObject.parse("inetnum: 193.0.0.0");

        when(ipv4Tree.findFirstMoreSpecific(parentIpv4Key)).thenReturn(Lists.<Ipv4Entry>newArrayList());
        when(update.getReferenceObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_no_children_ipv6() {
        final RpslObject object = RpslObject.parse("inet6num: 2001:0658:021A::/48");

        when(ipv6Tree.findFirstMoreSpecific(parentIpv6Key)).thenReturn(Lists.<Ipv6Entry>newArrayList());
        when(update.getReferenceObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_no_intersections_ipv4() {
        final RpslObject object = RpslObject.parse("inetnum: 193.0.0.0 - 193.0.0.10");

        when(ipv4Tree.findFirstMoreSpecific(parentIpv4Key)).thenReturn(Lists.newArrayList(
                new Ipv4Entry(Ipv4Resource.parse("192/8"), 1),
                new Ipv4Entry(Ipv4Resource.parse("193.0.0.1"), 2),
                new Ipv4Entry(Ipv4Resource.parse("193.0.1/24"), 3),
                new Ipv4Entry(Ipv4Resource.parse("193.0.0.1 - 193.0.0.2"), 4)
        ));

        when(update.getReferenceObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_intersections_ipv4() {
        final RpslObject object = RpslObject.parse("inetnum: 193.0.0.0 - 193.0.0.10");

        when(ipv4Tree.findFirstMoreSpecific(parentIpv4Key)).thenReturn(Lists.newArrayList(
                new Ipv4Entry(Ipv4Resource.parse("193.0.0.10 - 193.0.0.12"), 1),
                new Ipv4Entry(Ipv4Resource.parse("193.0.1/24"), 2)
        ));

        when(update.getReferenceObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.intersectingRange(Ipv4Resource.parse("193.0.0.10 - 193.0.0.12")));

        verifyNoMoreInteractions(updateContext);
    }
}
