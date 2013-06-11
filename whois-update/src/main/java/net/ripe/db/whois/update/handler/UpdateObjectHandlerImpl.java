package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.attrs.Changed;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
class UpdateObjectHandlerImpl implements UpdateObjectHandler {
    private final DateTimeProvider dateTimeProvider;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final Map<Action, Map<ObjectType, List<BusinessRuleValidator>>> validatorsByActionAndType;

    @Autowired
    public UpdateObjectHandlerImpl(final RpslObjectUpdateDao rpslObjectUpdateDao, final List<BusinessRuleValidator> businessRuleValidators,
                                   final DateTimeProvider dateTimeProvider) {
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.dateTimeProvider = dateTimeProvider;

        validatorsByActionAndType = Maps.newEnumMap(Action.class);
        for (final Action action : Action.values()) {
            final Map<ObjectType, List<BusinessRuleValidator>> validatorsByType = Maps.newEnumMap(ObjectType.class);
            for (final ObjectType objectType : ObjectType.values()) {
                validatorsByType.put(objectType, Lists.<BusinessRuleValidator>newArrayList());
            }

            validatorsByActionAndType.put(action, validatorsByType);
        }

        for (final BusinessRuleValidator businessRuleValidator : businessRuleValidators) {
            final List<Action> actions = businessRuleValidator.getActions();
            for (final Action action : actions) {
                for (final ObjectType objectType : businessRuleValidator.getTypes()) {
                    validatorsByActionAndType.get(action).get(objectType).add(businessRuleValidator);
                }
            }
        }
    }

    RpslObject updateLastChangedAttribute(final RpslObject submittedObject) {
        final List<RpslAttribute> attributes = submittedObject.findAttributes(AttributeType.CHANGED);
        if (attributes.isEmpty()) {
            return submittedObject;
        }

        final RpslAttribute attributeToUpdate = getChangedAttributeToUpdate(attributes);
        if (attributeToUpdate == null) {
            return submittedObject;
        }

        final ArrayList<RpslAttribute> rpslAttributes = Lists.newArrayList(submittedObject.getAttributes());
        final Changed newAttributeValue = new Changed(attributeToUpdate.getCleanValue(), dateTimeProvider.getCurrentDate());

        rpslAttributes.set(rpslAttributes.indexOf(attributeToUpdate), new RpslAttribute(AttributeType.CHANGED, newAttributeValue.toString()));
        return new RpslObject(submittedObject, rpslAttributes);
    }

    private RpslAttribute getChangedAttributeToUpdate(final List<RpslAttribute> attributes) {
        for (final RpslAttribute attribute : attributes) {
            final Changed changed = Changed.parse(attribute.getCleanValue());
            if (changed.getDate() == null) {
                return attribute;
            }
        }

        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void execute(final PreparedUpdate update, final UpdateContext updateContext) {
        if (!updateContext.hasErrors(update)) {
            switch (update.getAction()) {
                case CREATE:
                    rpslObjectUpdateDao.createObject(updateLastChangedAttribute(update.getUpdatedObject()));
                    break;
                case MODIFY:
                    rpslObjectUpdateDao.updateObject(update.getReferenceObject().getObjectId(), updateLastChangedAttribute(update.getUpdatedObject()));
                    break;
                case DELETE:
                    final RpslObject object = update.getReferenceObject();
                    rpslObjectUpdateDao.deleteObject(object.getObjectId(), object.getKey().toString());
                    break;
                case NOOP:
                    updateContext.addMessage(update, UpdateMessages.updateIsIdentical());
                    break;
                default:
                    throw new IllegalStateException("Unhandled action: " + update.getAction());
            }
        }
    }

    @Override
    public boolean validateBusinessRules(final PreparedUpdate update, final UpdateContext updateContext) {
        // TODO [AK] There must be a better way to set the status than to count errors
        final int initialErrorCount = updateContext.getErrorCount(update);

        final Action action = update.getAction();
        final Map<ObjectType, List<BusinessRuleValidator>> validatorsByType = validatorsByActionAndType.get(action);

        final ObjectType type = update.getType();
        final List<BusinessRuleValidator> validators = validatorsByType.get(type);
        for (final BusinessRuleValidator businessRuleValidator : validators) {
            businessRuleValidator.validate(update, updateContext);
        }

        return updateContext.getErrorCount(update) == initialErrorCount;
    }
}
