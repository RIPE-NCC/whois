package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AbuseCNoLimitWarningValidator implements BusinessRuleValidator {
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
        if (update.getUpdatedObject().containsAttribute(AttributeType.ABUSE_MAILBOX)
                && (!update.hasOriginalObject() || !update.getReferenceObject().containsAttribute(AttributeType.ABUSE_MAILBOX))) {
            updateContext.addMessage(update, UpdateMessages.abuseCNoLimitWarning());
        }
    }
}
