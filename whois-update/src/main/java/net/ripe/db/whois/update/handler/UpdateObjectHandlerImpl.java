package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

import java.util.*;

@Component
class UpdateObjectHandlerImpl implements UpdateObjectHandler {
    private static final HashSet<ObjectType> SUPPORTED_TYPES = Sets.newHashSet(
            ObjectType.AS_SET,
            ObjectType.AS_BLOCK,
            ObjectType.AUT_NUM,
            ObjectType.DOMAIN,
            ObjectType.FILTER_SET,
            ObjectType.INETNUM,
            ObjectType.INET6NUM,
            ObjectType.KEY_CERT,
            ObjectType.INET_RTR,
            ObjectType.IRT,
            ObjectType.MNTNER,
            ObjectType.ORGANISATION,
            ObjectType.PEERING_SET,
            ObjectType.PERSON,
            ObjectType.POEM,
            ObjectType.POETIC_FORM,
            ObjectType.ROLE,
            ObjectType.ROUTE,
            ObjectType.ROUTE6,
            ObjectType.ROUTE_SET,
            ObjectType.RTR_SET);

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
        if (isValid(update, updateContext)) {
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

    private boolean isValid(final PreparedUpdate update, final UpdateContext updateContext) {
        for (final BusinessRuleValidator businessRuleValidator : validatorsByActionAndType.get(update.getAction()).get(update.getType())) {
            businessRuleValidator.validate(update, updateContext);
        }

        return !updateContext.hasErrors(update);
    }

    @Override
    public Set<ObjectType> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }
}
