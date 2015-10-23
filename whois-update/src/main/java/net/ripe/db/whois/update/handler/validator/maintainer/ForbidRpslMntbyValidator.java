package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.Lists;

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
public class ForbidRpslMntbyValidator implements BusinessRuleValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        if (containsRpslMntner(update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.rpslMntbyForbidden());
        }
    }

    private boolean containsRpslMntner(final RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(AttributeType.MNT_BY)) {
            return false;
        }

        for (RpslAttribute mntBy : rpslObject.findAttributes(AttributeType.MNT_BY)) {
            if (mntBy.getCleanValue().equals("RIPE-NCC-RPSL-MNT")) {
                return true;
            }
        }
        return false;
    }



}
