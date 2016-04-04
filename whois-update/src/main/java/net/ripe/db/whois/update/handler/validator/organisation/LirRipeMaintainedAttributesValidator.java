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
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
// Validates that RIPE NCC maintained attributes are not changed for an LIR
public class LirRipeMaintainedAttributesValidator implements BusinessRuleValidator {

    private final LoggerContext loggerContext;

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final CIString LIR = CIString.ciString("LIR");
    private static final ImmutableList<AttributeType> ATTRIBUTES = ImmutableList.of(
            AttributeType.ADDRESS,
            AttributeType.PHONE,
            AttributeType.FAX_NO,
            AttributeType.E_MAIL,
            AttributeType.ORG_NAME);

    @Autowired
    public LirRipeMaintainedAttributesValidator(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            log(update, "organisation update with override");
            return;
        }

        final RpslObject originalObject = update.getReferenceObject();
        if (!LIR.equals(originalObject.getValueForAttribute(AttributeType.ORG_TYPE))) {
            log(update, "organisation update is not for an LIR");
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        ATTRIBUTES.forEach(attributeType -> {
            if (orgAttributeChanged(originalObject, updatedObject, attributeType)) {
                log(update, "organisation update has ripe maintained attribute");
                updateContext.addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(attributeType));
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

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
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
