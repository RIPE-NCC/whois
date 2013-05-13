package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.attrs.InetnumStatus;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubAllocationValidator implements BusinessRuleValidator {
    private static final int SUB_ALLOCATED_PA_MAX_PREFIX = 24;

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INETNUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString statusValue = updatedObject.getValueForAttribute(AttributeType.STATUS);
        if (InetnumStatus.SUB_ALLOCATED_PA.equals(InetStatusHelper.getStatus(statusValue, updatedObject))) {
            final Ipv4Resource ipv4Resource = Ipv4Resource.parse(updatedObject.getKey());
            if (ipv4Resource.getPrefixLength() > SUB_ALLOCATED_PA_MAX_PREFIX) {
                updateContext.addMessage(update, UpdateMessages.rangeTooSmallForStatus(InetnumStatus.SUB_ALLOCATED_PA, SUB_ALLOCATED_PA_MAX_PREFIX));
            }
        }
    }
}
