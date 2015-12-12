package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObjectMismatchValidator implements BusinessRuleValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.hasOriginalObject()
                && !RpslObjectFilter.ignoreGeneratedAttributesEqual(update.getReferenceObject(), update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.objectMismatch(update.getUpdatedObject().getFormattedKey()));
        }
    }
}