package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
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

import java.util.Collections;
import java.util.List;

@Component
public abstract class AbstractRemarksValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final AttributeType attribute = getAttributeType();
        if(updatedObject== null || !updatedObject.containsAttribute(attribute)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        for (CIString remarks : updatedObject.getValuesForAttribute(AttributeType.REMARKS)) {
            if(remarks.startsWith(attribute.getName().concat(":"))) {
                messages.add(UpdateMessages.eitherAttributeOrRemarksIsAllowed(attribute.getName()));
                break;
            }
        }

        return messages;
    }

    protected abstract AttributeType getAttributeType();

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
