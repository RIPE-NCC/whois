package net.ripe.db.whois.update.handler.validator.route;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValueWithinPrefixValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @InjectMocks ValueWithinPrefixValidator subject;

    @Test
    public void testGetActions() throws Exception {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void testGetTypes() throws Exception {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.ROUTE, ObjectType.ROUTE6));
    }

    @Test
    public void route_with_one_holes_attribute_outside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:195.190.20.0/24\norigin:AS43746\nholes:94.73.128.0/24");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("94.73.128.0/24"));
    }

    @Test
    public void route_with_many_holes_attributes_outside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:195.190.20.0/24\norigin:AS43746\nholes:94.73.128.0/24\nholes:94.73.134.0/24");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        List<RpslAttribute> rpslAttributes = route.findAttributes(AttributeType.HOLES);

        verify(updateContext, times(1)).addMessage(update, rpslAttributes.get(0), UpdateMessages.invalidRouteRange("94.73.128.0/24"));
        verify(updateContext, times(1)).addMessage(update, rpslAttributes.get(1), UpdateMessages.invalidRouteRange("94.73.134.0/24"));
    }

    @Test
    public void route_with_holes_attribute_list_outside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:195.190.20.0/24\norigin:AS43746\nholes:94.73.128.0/24,94.73.134.0/24");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("94.73.128.0/24"));
        verify(updateContext, times(1)).addMessage(update, route.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("94.73.134.0/24"));
    }

    @Test
    public void route_with_holes_attribute_list_inside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:94.73.128.0/18\norigin:AS43746\nholes:94.73.129.0/24, 94.73.131.0/24\nholes:94.73.137.0/24");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route6_with_one_holes_attribute_outside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2a98::/32\norigin:AS43746\nholes:2a01:568:4000::/36");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route6.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("2a01:568:4000::/36"));
    }

    @Test
    public void route6_with_many_holes_attributes_outside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2a98::/32\norigin:AS43746\nholes:2a01:758:4000::/48\nholes:2a01:758:5000::/48");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        List<RpslAttribute> rpslAttributes = route6.findAttributes(AttributeType.HOLES);

        verify(updateContext, times(1)).addMessage(update, rpslAttributes.get(0), UpdateMessages.invalidRouteRange("2a01:758:4000::/48"));
        verify(updateContext, times(1)).addMessage(update, rpslAttributes.get(1), UpdateMessages.invalidRouteRange("2a01:758:5000::/48"));
    }

    @Test
    public void route6_with_holes_attribute_list_outside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2a98::/32\norigin:AS43746\nholes:2a01:758:4000::/48,2a01:758:5000::/48");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route6.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("2a01:758:4000::/48"));
        verify(updateContext, times(1)).addMessage(update, route6.findAttribute(AttributeType.HOLES), UpdateMessages.invalidRouteRange("2a01:758:5000::/48"));
    }

    @Test
    public void route6_with_holes_attribute_list_inside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2020::/32\norigin:AS43746\nholes:2A02:2020:0000:0000::/48,2A02:2020:0001:0000::/48\nholes:2A02:2020:0002:0000::/48");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }


    @Test
    public void route6_with_one_pingable_attribute_outside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2a98::/32\norigin:AS43746\npingable:2a00:9e80::1");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route6.findAttribute(AttributeType.PINGABLE), UpdateMessages.invalidRouteRange("2a00:9e80::1"));
    }

    @Test
    public void route6_with_many_pingable_attributes_outside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a02:2a98::/32\norigin:AS43746\npingable:2a00:1e88:e::4\npingable:2a00:1e88:e::5");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        List<RpslAttribute> pingableAttribute = route6.findAttributes(AttributeType.PINGABLE);

        verify(updateContext, times(1)).addMessage(update, pingableAttribute.get(0), UpdateMessages.invalidRouteRange("2a00:1e88:e::4"));
        verify(updateContext, times(1)).addMessage(update, pingableAttribute.get(1), UpdateMessages.invalidRouteRange("2a00:1e88:e::5"));
    }

    @Test
    public void route6_with_pingable_attribute_list_inside_prefixrange() {
        final RpslObject route6 = RpslObject.parse("route6:2a00:1e88::/32\norigin:AS43746\npingable:2a00:1e88:e::4\npingable:2a00:1e88:e::5");
        when(update.getUpdatedObject()).thenReturn(route6);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route_with_one_pingable_attribute_outside_prefixrange() {
        final RpslObject route = RpslObject.parse("route: 84.40.47.0/24\norigin:AS43746\npingable:93.191.209.1");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, route.findAttribute(AttributeType.PINGABLE), UpdateMessages.invalidRouteRange("93.191.209.1"));
    }

    @Test
    public void route_with_many_pingable_attributes_outside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:84.40.47.0/24\norigin:AS43746\npingable:95.180.201.1\npingable:93.191.209.1");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        List<RpslAttribute> pingableAttribute = route.findAttributes(AttributeType.PINGABLE);

        verify(updateContext, times(1)).addMessage(update, pingableAttribute.get(0), UpdateMessages.invalidRouteRange("95.180.201.1"));
        verify(updateContext, times(1)).addMessage(update, pingableAttribute.get(1), UpdateMessages.invalidRouteRange("93.191.209.1"));
    }

    @Test
    public void route_with_pingable_attribute_list_inside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:93.191.209.0/24\norigin:AS43746\npingable:93.191.209.1\npingable:93.191.209.2");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route_with_pingable_and_holes_attribute_list_inside_prefixrange() {
        final RpslObject route = RpslObject.parse("route:93.191.209.0/24\norigin:AS43746\npingable:93.191.209.1\npingable:93.191.209.2\nholes:93.191.209.0/31");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route_too_large() {
        final RpslObject route = RpslObject.parse("route:93.191.209.0/7\norigin:AS43746");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidRoutePrefix("route"));
    }

    @Test
    public void route_not_too_large() {
        final RpslObject route = RpslObject.parse("route:93.191.209.0/8\norigin:AS43746");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route6_too_large() {
        final RpslObject route = RpslObject.parse("route6:2a02:2a98::/11\norigin:AS43746");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidRoutePrefix("route6"));
    }

    @Test
    public void route6_not_too_large() {
        final RpslObject route = RpslObject.parse("route6:2a02:2a98::/12\norigin:AS43746");
        when(update.getUpdatedObject()).thenReturn(route);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
