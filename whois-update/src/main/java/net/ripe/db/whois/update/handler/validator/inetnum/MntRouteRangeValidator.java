package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MntRouteRangeValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final IpInterval<?> ipInterval = IpInterval.parse(updatedObject.getKey());

        final List<CustomValidationMessage> messages = Lists.newArrayList();
        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.MNT_ROUTES)) {
            final MntRoutes mntRoutes = MntRoutes.parse(attribute.getCleanValue());
            if (mntRoutes.isAnyRange()) {
                return Collections.emptyList();
            }

            for (final AddressPrefixRange addressPrefixRange : mntRoutes.getAddressPrefixRanges()) {
                final AddressPrefixRange.BoundaryCheckResult boundaryCheckResult = addressPrefixRange.checkWithinBounds(ipInterval);
                switch (boundaryCheckResult) {
                    case IPV4_EXPECTED:
                        messages.add(new CustomValidationMessage(UpdateMessages.invalidIpv6Address(addressPrefixRange.getIpInterval().toString()), attribute));
                        break;
                    case IPV6_EXPECTED:
                        messages.add(new CustomValidationMessage(UpdateMessages.invalidIpv4Address(addressPrefixRange.getIpInterval().toString()), attribute));
                        break;
                    case NOT_IN_BOUNDS:
                        messages.add(new CustomValidationMessage(UpdateMessages.invalidRouteRange(addressPrefixRange.toString()), attribute));
                        break;
                    default:
                        break;
                }
            }
        }

      return messages;
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
