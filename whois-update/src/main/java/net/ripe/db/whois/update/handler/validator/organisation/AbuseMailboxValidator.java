package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.IRT;
import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class AbuseMailboxValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE, MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(IRT, MNTNER, ORGANISATION, PERSON);

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public void validate(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        boolean abuseMailbox = updatedObject.containsAttribute(AttributeType.ABUSE_MAILBOX);

        if (abuseMailbox &&
                (update.getAction() == CREATE || !update.getReferenceObject().containsAttribute(AttributeType.ABUSE_MAILBOX))) {
            updateContext.addMessage(update, UpdateMessages.abuseMailboxCantBeAdded(updatedObject.getType()));
        }
    }
}
