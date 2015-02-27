package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TimestampsValidator implements BusinessRuleValidator {

    private final TimestampsMode timestampsMode;

    @Autowired
    public TimestampsValidator(final TimestampsMode timestampsMode) {
        this.timestampsMode = timestampsMode;
    }

    @Override
    public List<Action> getActions() {
        return Collections.unmodifiableList(Lists.newArrayList(Action.values()));
    }

    @Override
    public List<ObjectType> getTypes() {
        return Collections.unmodifiableList(Lists.newArrayList(ObjectType.values()));
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (timestampsMode.isTimestampsOff()) {
            if (update.getSubmittedObject().containsAttribute(AttributeType.CREATED)) {
                updateContext.addMessage(update, ValidationMessages.unknownAttribute(AttributeType.CREATED.getName()));
            }

            if (update.getSubmittedObject().containsAttribute(AttributeType.LAST_MODIFIED)) {
                updateContext.addMessage(update, ValidationMessages.unknownAttribute(AttributeType.LAST_MODIFIED.getName()));
            }
        }
    }
}
