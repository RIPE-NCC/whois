package net.ripe.db.whois.update.handler.validator.sets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PeeringSetAttributeMustBePresent implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.PEERING_SET, ObjectType.FILTER_SET);

    private Map<ObjectType, List<AttributeType>> attributeMap;


    public PeeringSetAttributeMustBePresent() {
        attributeMap = new HashMap<>();
        attributeMap.put(ObjectType.PEERING_SET, Lists.newArrayList(AttributeType.PEERING, AttributeType.MP_PEERING));
        attributeMap.put(ObjectType.FILTER_SET, Lists.newArrayList(AttributeType.FILTER, AttributeType.MP_FILTER));
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final ObjectType objectType = update.getType();
        final List<AttributeType> attributeTypes = attributeMap.get(objectType);

        final AttributeType simpleAttribute = attributeTypes.get(0);
        final List<RpslAttribute> simpleAttributes = updatedObject.findAttributes(simpleAttribute);
        final AttributeType complexAttribute = attributeTypes.get(1);
        final List<RpslAttribute> extendedAttributes = updatedObject.findAttributes(complexAttribute);

        final List<Message> messages = Lists.newArrayList();
        if (simpleAttributes.isEmpty() && extendedAttributes.isEmpty()) {
            messages.add(UpdateMessages.neitherSimpleOrComplex(objectType, simpleAttribute.getName(), complexAttribute.getName()));
        }

        if (!simpleAttributes.isEmpty() && !extendedAttributes.isEmpty()) {
            messages.add(UpdateMessages.eitherSimpleOrComplex(objectType, simpleAttribute.getName(), complexAttribute.getName()));
        }

        return messages;
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
