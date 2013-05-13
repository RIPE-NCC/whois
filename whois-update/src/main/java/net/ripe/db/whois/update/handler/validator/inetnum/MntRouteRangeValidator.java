package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.domain.attrs.MntRoutes;
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
public class MntRouteRangeValidator implements BusinessRuleValidator {
    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final IpInterval<?> ipInterval = IpInterval.parse(updatedObject.getKey());

        updatedObject.forAttributes(AttributeType.MNT_ROUTES, new RpslObject.AttributeCallback() {
            @Override
            public void execute(final RpslAttribute attribute, final CIString value) {
                final MntRoutes mntRoutes = MntRoutes.parse(value);
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
        });
    }
}
