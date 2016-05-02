package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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

@Component
// Validates that RIPE NCC maintained attributes are not changed for an LIR
public class LirRipeMaintainedAttributesValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final CIString LIR = CIString.ciString("LIR");
    private static final ImmutableList<AttributeType> ATTRIBUTES = ImmutableList.of(
            AttributeType.ADDRESS,
            AttributeType.PHONE,
            AttributeType.FAX_NO,
            AttributeType.E_MAIL,
            AttributeType.ORG_NAME);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final RpslObject originalObject = update.getReferenceObject();
        if (!LIR.equals(originalObject.getValueForAttribute(AttributeType.ORG_TYPE))) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        ATTRIBUTES.forEach(attributeType -> {
            if (orgAttributeChanged(originalObject, updatedObject, attributeType)) {
                updateContext.addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(attributeType));
            }
        });
    }

    private boolean orgAttributeChanged(final RpslObject originalObject,
                                        final RpslObject updatedObject,
                                        final AttributeType attributeType) {
        return !Iterables.elementsEqual(
                Iterables.filter(originalObject.getValuesForAttribute(attributeType), CIString.class),
                Iterables.filter(updatedObject.getValuesForAttribute(attributeType), CIString.class));
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
