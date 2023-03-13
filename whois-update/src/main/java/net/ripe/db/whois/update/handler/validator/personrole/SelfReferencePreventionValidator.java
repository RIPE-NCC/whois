package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
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

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROLE);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> messages = Lists.newArrayList();

        errorOnSelfReference(update, AttributeType.ADMIN_C, messages);
        errorOnSelfReference(update, AttributeType.TECH_C, messages);

        return messages;
    }

    private void errorOnSelfReference(final PreparedUpdate update, final AttributeType attributeType, final List<Message> messages) {
        final List<RpslAttribute> submittedAttributes = update.getUpdate().getSubmittedObject().findAttributes(attributeType);
        final CIString submittedNicHdl = update.getUpdate().getSubmittedObject().getValueForAttribute(AttributeType.NIC_HDL);

        for (final RpslAttribute attribute : submittedAttributes) {
            if (attribute.getCleanValues().contains(submittedNicHdl)) {
                messages.add(UpdateMessages.selfReferenceError(attribute));
            }
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

