package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelfReferencePreventionValidator implements BusinessRuleValidator {
    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ROLE);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        errorOnSelfReference(update, updateContext, AttributeType.ADMIN_C);
        errorOnSelfReference(update, updateContext, AttributeType.TECH_C);
    }

    private void errorOnSelfReference(final PreparedUpdate update, final UpdateContext updateContext, final AttributeType attributeType) {
        final List<RpslAttribute> submittedAttributes = update.getUpdate().getSubmittedObject().findAttributes(attributeType);
        final CIString submittedNicHdl = update.getUpdate().getSubmittedObject().getValueForAttribute(AttributeType.NIC_HDL);

        for (final RpslAttribute attribute : submittedAttributes) {
            if (attribute.getCleanValues().contains(submittedNicHdl)) {
                updateContext.addMessage(update, attribute, UpdateMessages.selfReferenceError(attributeType));
            }
        }
    }

}

