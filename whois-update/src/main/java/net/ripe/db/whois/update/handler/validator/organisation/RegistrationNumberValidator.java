package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationNumberValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> messages = Lists.newArrayList();

        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return messages;
        }

        final RpslObject referenceObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        if(referenceObject == null || updatedObject == null) {
            return messages;
        }

        if(update.getAction() == Action.MODIFY) {

            if (wasAttributeAddedOrRemoved(referenceObject, updatedObject, AttributeType.REG_NR)) {
                messages.add(UpdateMessages.cantAddorRemoveRipeNccRegNr());
            }

            final CIString referenceRegNr = referenceObject.getValueOrNullForAttribute(AttributeType.REG_NR);
            final CIString updatedRegNr = updatedObject.getValueOrNullForAttribute(AttributeType.REG_NR);
            if (referenceRegNr != null && updatedRegNr != null && !updatedRegNr.equals(referenceRegNr)) {
                messages.add(UpdateMessages.regNrChanged(updatedObject.findAttribute(AttributeType.REG_NR)));
            }

            return messages;
        }

        if(referenceObject.containsAttribute(AttributeType.REG_NR)) {
            messages.add(UpdateMessages.cantAddorRemoveRipeNccRegNr());
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

    @Override
    public boolean isSkipForOverride() {
        return true;
    }
}
