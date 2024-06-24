package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TooManyReferencesValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private final int maximumReferences;

    @Autowired
    public TooManyReferencesValidator(@Value("${max.references:100:}")  final int maximumReferences) {
        this.maximumReferences = maximumReferences;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> messages = Lists.newArrayList();
        final RpslObject updatedObject = update.getUpdatedObject();

        int references = 0;
        for (RpslAttribute attribute : updatedObject.getAttributes()) {
            if (attribute.getType().isReference() && (++references > maximumReferences)) {
                messages.add(UpdateMessages.tooManyReferences());
                break;
            }
        }

        return messages;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

}
