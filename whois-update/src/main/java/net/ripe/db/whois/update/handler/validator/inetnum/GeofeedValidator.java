package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.GEOFEED;
import static net.ripe.db.whois.common.rpsl.AttributeType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INETNUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.REMARKS;

@Component
public class GeofeedValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);
    private static final int IPV4_MAXIMUM_PREFIX_SIZE = 24;
    private static final int IPV6_MAXIMUM_PREFIX_SIZE = 48;

    @Override
    public List<CustomValidationMessage> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        if(!updatedObject.containsAttribute(GEOFEED)) {
            return Collections.emptyList();
        }

        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();
        for (CIString remarks : updatedObject.getValuesForAttribute(REMARKS)) {
            if(remarks.startsWith("geofeed:")) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.eitherGeofeedOrRemarksIsAllowed()));
                break;
            }
        }

        if(ObjectType.INETNUM == updatedObject.getType()) {
            final Ipv4Resource ipv4Resource = Ipv4Resource.parse(updatedObject.getValueForAttribute(INETNUM));

            if(ipv4Resource.getPrefixLength() > IPV4_MAXIMUM_PREFIX_SIZE) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.geofeedTooSpecific(IPV4_MAXIMUM_PREFIX_SIZE)));
            }
        } else if(ObjectType.INET6NUM == updatedObject.getType()) {
            final Ipv6Resource ipv6Resource = Ipv6Resource.parse(updatedObject.getValueForAttribute(INET6NUM));

            if(ipv6Resource.getPrefixLength() >= IPV6_MAXIMUM_PREFIX_SIZE) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.geofeedTooSpecific(IPV6_MAXIMUM_PREFIX_SIZE)));
            }
        }

        return customValidationMessages;
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
