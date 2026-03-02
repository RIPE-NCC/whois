package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
public class JustFreeTextUtf8Validator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final ImmutableList<String> FREE_TEXT_ATTRIBUTES = ImmutableList.of(AttributeType.DESCR.getName(), AttributeType.REMARKS.getName());

    private static final int MAX_ASCII_RANGE_CODE = 127;

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null || !hasInvalidUtf8(updatedObject)) {
            return Collections.emptyList();
        }

        return List.of(UpdateMessages.notSupportUtf8Attribute());
    }

    public static boolean hasInvalidUtf8(RpslObject object) {
        for (RpslAttribute attribute : object.getAttributes()) {
            if (!FREE_TEXT_ATTRIBUTES.contains(attribute.getKey())) {
                if (attribute.getValue().codePoints().anyMatch(cp -> cp > MAX_ASCII_RANGE_CODE)) {
                    return true;
                }
            }
        }
        return false;
    }
}
