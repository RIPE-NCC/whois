package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

@Component
public class ObjectMismatchValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.hasOriginalObject()
                && !RpslObjectFilter.ignoreGeneratedAttributesEqual(update.getReferenceObject(), update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.objectMismatch(update.getUpdatedObject().getFormattedKey()));
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