package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
//TODO TP: Remove validator when timestamps always on.
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
            final RpslObject submittedObject = update.getSubmittedObject();
            if (submittedObject.containsAttribute(AttributeType.CREATED)) {
                updateContext.addMessage(update, submittedObject.findAttributes(AttributeType.CREATED).get(0), ValidationMessages.unknownAttribute(AttributeType.CREATED.getName()));
            }

            if (submittedObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                updateContext.addMessage(update, submittedObject.findAttributes(AttributeType.LAST_MODIFIED).get(0), ValidationMessages.unknownAttribute(AttributeType.LAST_MODIFIED.getName()));
            }
        }
    }
}
