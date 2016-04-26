package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
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

@Component
public class ForbidRpslMntbyValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final String RIPE_NCC_RPSL_MNT = "RIPE-NCC-RPSL-MNT";

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
            for (CIString mntByStr : mntBy.getCleanValues()) {
                if (mntByStr.equals(RIPE_NCC_RPSL_MNT)) {
                    return true;
                }
            }
        }
        return false;
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
