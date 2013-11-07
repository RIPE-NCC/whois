package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValueWithinPrefixValidator implements BusinessRuleValidator {
    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ROUTE, ObjectType.ROUTE6);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedRouteObject = update.getUpdatedObject();
        final AttributeType attributeType = findAttributeType(updatedRouteObject);

        final CIString prefix = updatedRouteObject.findAttribute(attributeType).getCleanValue();
        for (final RpslAttribute holeAttribute : updatedRouteObject.findAttributes(AttributeType.HOLES)) {
            for (final CIString hole : holeAttribute.getCleanValues()) {
                validatePrefixForHolesAttribute(update, updateContext, AddressPrefixRange.parse(hole), prefix, holeAttribute);
            }
        }

        for (final RpslAttribute pingableAttribute : updatedRouteObject.findAttributes(AttributeType.PINGABLE)) {
            for (final CIString pingable : pingableAttribute.getCleanValues()) {
                validatePrefixForPingableAttribute(update, updateContext, pingable, prefix, pingableAttribute);
            }
        }

        final IpInterval ipInterval = IpInterval.parse(prefix);
        if ((ipInterval.getPrefixLength() < 8 && attributeType == AttributeType.ROUTE) ||
             (ipInterval.getPrefixLength() < 12 && attributeType == AttributeType.ROUTE6)) {
            updateContext.addMessage(update, UpdateMessages.invalidRoutePrefix(attributeType.getName()));
        }
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

    private void validatePrefixForHolesAttribute(final PreparedUpdate update, final UpdateContext updateContext, final AddressPrefixRange addressPrefixRange, final CIString ip, final RpslAttribute rpslAttribute) {
        final AddressPrefixRange.BoundaryCheckResult boundaryCheckResult = addressPrefixRange.checkWithinBounds(IpInterval.parse(ip));
        switch (boundaryCheckResult) {
            case IPV6_EXPECTED:
                updateContext.addMessage(update, UpdateMessages.invalidIpv4Address(addressPrefixRange.getIpInterval().toString()));
                break;
            case IPV4_EXPECTED:
                updateContext.addMessage(update, UpdateMessages.invalidIpv6Address(addressPrefixRange.getIpInterval().toString()));
                break;
            case NOT_IN_BOUNDS:
                updateContext.addMessage(update, rpslAttribute, UpdateMessages.invalidRouteRange(addressPrefixRange.toString()));
                break;
            default:
                break;
        }
    }

    private void validatePrefixForPingableAttribute(final PreparedUpdate update, final UpdateContext updateContext, final CIString pingableIp, final CIString ip, final RpslAttribute rpslAttribute) {
        final IpInterval ipInterval = IpInterval.parse(ip);
        if (!ipInterval.contains(IpInterval.parse(pingableIp))) {
            updateContext.addMessage(update, rpslAttribute, UpdateMessages.invalidRouteRange(pingableIp));
        }
    }
}
