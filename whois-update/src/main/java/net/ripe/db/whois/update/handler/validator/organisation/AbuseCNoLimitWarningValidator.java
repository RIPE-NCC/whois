package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

@Component
public class AbuseCNoLimitWarningValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROLE);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getUpdatedObject().containsAttribute(AttributeType.ABUSE_MAILBOX)
                && (!update.hasOriginalObject() || !update.getReferenceObject().containsAttribute(AttributeType.ABUSE_MAILBOX))) {
            updateContext.addMessage(update, UpdateMessages.abuseCNoLimitWarning());
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
