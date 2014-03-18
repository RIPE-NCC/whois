package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TypeAttributeModifyNotAllowedValidator implements BusinessRuleValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.PERSON, ObjectType.ROLE);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final CIString originalValue = update.getReferenceObject().getTypeAttribute().getCleanValue();
        final CIString updatedValue = update.getUpdatedObject().getTypeAttribute().getCleanValue();

        if (!originalValue.equals(updatedValue)) {
            updateContext.addMessage(update, UpdateMessages.nameChanged());
        }
    }
}
