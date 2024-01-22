package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AddressPrefixRange;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValueWithinPrefixValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedRouteObject = update.getUpdatedObject();
        final AttributeType attributeType = findAttributeType(updatedRouteObject);

        final List<Message> messages = Lists.newArrayList();

        final CIString prefix = updatedRouteObject.findAttribute(attributeType).getCleanValue();
        for (final RpslAttribute holeAttribute : updatedRouteObject.findAttributes(AttributeType.HOLES)) {
            for (final CIString hole : holeAttribute.getCleanValues()) {
                validatePrefixForHolesAttribute(AddressPrefixRange.parse(hole), prefix, holeAttribute, messages);
            }
        }

        for (final RpslAttribute pingableAttribute : updatedRouteObject.findAttributes(AttributeType.PINGABLE)) {
            for (final CIString pingable : pingableAttribute.getCleanValues()) {
                validatePrefixForPingableAttribute(pingable, prefix, pingableAttribute, messages);
            }
        }

        final IpInterval ipInterval = IpInterval.parse(prefix);
        if ((ipInterval.getPrefixLength() < 8 && attributeType == AttributeType.ROUTE) ||
             (ipInterval.getPrefixLength() < 12 && attributeType == AttributeType.ROUTE6)) {
            messages.add(UpdateMessages.invalidRoutePrefix(attributeType.getName()));
        }

        return messages;
    }

    private AttributeType findAttributeType(final RpslObject updatedRouteObject) {
        AttributeType attributeType;
        switch (updatedRouteObject.getType()) {
            case ROUTE:
                attributeType = AttributeType.ROUTE;
                break;
            case ROUTE6:
                attributeType = AttributeType.ROUTE6;
                break;
            default:
                throw new IllegalArgumentException("can't validate other than route / route6 objects");
        }
        return attributeType;
    }

    private void validatePrefixForHolesAttribute(final AddressPrefixRange addressPrefixRange, final CIString ip, final RpslAttribute rpslAttribute, final List<Message> messages) {
        final AddressPrefixRange.BoundaryCheckResult boundaryCheckResult = addressPrefixRange.checkWithinBounds(IpInterval.parse(ip));
        switch (boundaryCheckResult) {
            case IPV6_EXPECTED:
                messages.add(UpdateMessages.invalidIpv4Address(addressPrefixRange.getIpInterval().toString()));
                break;
            case IPV4_EXPECTED:
                messages.add(UpdateMessages.invalidIpv6Address(addressPrefixRange.getIpInterval().toString()));
                break;
            case NOT_IN_BOUNDS:
                messages.add(UpdateMessages.invalidRouteRange(rpslAttribute, addressPrefixRange.toString()));
                break;
            default:
                break;
        }
    }

    private void validatePrefixForPingableAttribute(final CIString pingableIp, final CIString ip, final RpslAttribute rpslAttribute, final List<Message> messages) {
        final IpInterval ipInterval = IpInterval.parse(ip);
        final IpInterval pingableInterval = IpInterval.parse(pingableIp);

        if ((ipInterval instanceof Ipv4Resource) && !(pingableInterval instanceof Ipv4Resource)) {
            messages.add(UpdateMessages.invalidIpv4Address(rpslAttribute,pingableIp));
            return;
        }

        if ((ipInterval instanceof Ipv6Resource) && !(pingableInterval instanceof Ipv6Resource)) {
            messages.add(UpdateMessages.invalidIpv6Address(rpslAttribute, pingableIp));
            return;
        }

        if (!ipInterval.contains(pingableInterval)) {
            messages.add(UpdateMessages.invalidRouteRange(rpslAttribute, pingableIp));
        }
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
