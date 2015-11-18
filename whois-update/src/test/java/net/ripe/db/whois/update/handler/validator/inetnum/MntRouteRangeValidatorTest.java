package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MntRouteRangeValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @InjectMocks MntRouteRangeValidator subject;

    @Test
    public void is_inside_range() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.255.255\n" +
                "mnt-routes: DEV-MNT {192.0.0.0/24}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void is_inside_range_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: QSC-NOC {2a00:c00::/48}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void is_inside_range_multiple() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.255.255\n" +
                "mnt-routes: DEV-MNT1 {192.0.0.0/24}\n" +
                "mnt-routes: DEV-MNT2 {192.0.0.0/32}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void is_inside_range_multiple_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: QSC-NOC {2a00:c00::/48}\n" +
                "mnt-routes: QSC-NOC {2a00:c00::/64}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void any_range() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.255.255\n" +
                "mnt-routes: DEV-MNT ANY");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void any_range_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: DEV-MNT ANY");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void ipv6_range_with_ipv4() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.255.255\n" +
                "mnt-routes: DEV-MNT {::0/128^+}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidIpv4Address("::/128")));
    }

    @Test
    public void ipv4_range_with_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: DEV-MNT {192.0.0.0/24}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidIpv6Address("192.0.0.0/24")));
    }

    @Test
    public void is_outside_range() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.0.255\n" +
                "mnt-routes: DEV-MNT {192.0.0.0/16}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("192.0.0.0/16")));
    }

    @Test
    public void is_outside_range_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/16}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("2a00:c00::/16")));
    }

    @Test
    public void is_outside_range_multiple() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:192.0.0.0 - 192.0.0.255\n" +
                "mnt-routes: DEV-MNT {192.0.0.0/16,192.0.0.0/8}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("192.0.0.0/16")));
        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("192.0.0.0/8")));
    }

    @Test
    public void is_outside_range_multiple_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:2a00:c00::/32\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/24,2a00:c00::/16}");

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("2a00:c00::/24")));
        verify(updateContext).addMessage(any(PreparedUpdate.class), eq(rpslObject.findAttribute(AttributeType.MNT_ROUTES)), eq(UpdateMessages.invalidRouteRange("2a00:c00::/16")));
    }

    @Test
    public void supports() {
        final List<ObjectType> supportedTypes = subject.getTypes();

        assertThat(supportedTypes, containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void supports_actions_create_modify() {
        final List<Action> actions = subject.getActions();

        assertThat(actions.size(), is(2));
        assertThat(actions.contains(Action.CREATE), is(true));
        assertThat(actions.contains(Action.MODIFY), is(true));
    }
}
