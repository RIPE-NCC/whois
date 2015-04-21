package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObjectMismatchValidator implements BusinessRuleValidator {

    //TODO MG: Remove when timestamps always on.
    private final TimestampsMode timestampsMode;

    @Autowired
    public ObjectMismatchValidator(final TimestampsMode timestampsMode) {
        this.timestampsMode = timestampsMode;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        if (timestampsMode.isTimestampsOff() && update.getAction() == Action.DELETE) {
            RpslObject referenced = stripCreatedLastModified(update.getReferenceObject());
            RpslObject submitted = stripCreatedLastModified(update.getUpdatedObject());
            if ( !referenced.equals(submitted)) {
                updateContext.addMessage(update, UpdateMessages.objectMismatch(update.getUpdatedObject().getFormattedKey()));
            }
        } else {
            if (update.hasOriginalObject() && !update.getReferenceObject().equals(update.getUpdatedObject())) {
                updateContext.addMessage(update, UpdateMessages.objectMismatch(update.getUpdatedObject().getFormattedKey()));
            }
        }
    }

    private RpslObject stripCreatedLastModified(RpslObject input) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(input);
        builder.removeAttributeType(AttributeType.CREATED);
        builder.removeAttributeType(AttributeType.LAST_MODIFIED);
        return builder.get();
    }
}