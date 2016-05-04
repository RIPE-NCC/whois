package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

@Component
public class MntRouteRangeValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final IpInterval<?> ipInterval = IpInterval.parse(updatedObject.getKey());

        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.MNT_ROUTES)) {
            final MntRoutes mntRoutes = MntRoutes.parse(attribute.getCleanValue());
            if (mntRoutes.isAnyRange()) {
                return;
            }

            for (final AddressPrefixRange addressPrefixRange : mntRoutes.getAddressPrefixRanges()) {
                final AddressPrefixRange.BoundaryCheckResult boundaryCheckResult = addressPrefixRange.checkWithinBounds(ipInterval);
                switch (boundaryCheckResult) {
                    case IPV4_EXPECTED:
                        updateContext.addMessage(update, attribute, UpdateMessages.invalidIpv6Address(addressPrefixRange.getIpInterval().toString()));
                        break;
                    case IPV6_EXPECTED:
                        updateContext.addMessage(update, attribute, UpdateMessages.invalidIpv4Address(addressPrefixRange.getIpInterval().toString()));
                        break;
                    case NOT_IN_BOUNDS:
                        updateContext.addMessage(update, attribute, UpdateMessages.invalidRouteRange(addressPrefixRange.toString()));
                        break;
                    default:
                        break;
                }
            }
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
