package net.ripe.db.whois.update.handler.validator.sets;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.authentication.strategy.MntByAuthentication;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class SetnameMustExistValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.AS_SET, ObjectType.FILTER_SET, ObjectType.PEERING_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET);

    private final RpslObjectDao objectDao;
    private final AuthenticationModule authenticationModule;

    @Autowired
    public SetnameMustExistValidator(final RpslObjectDao objectDao,
                                     final AuthenticationModule authenticationModule) {
        this.objectDao = objectDao;
        this.authenticationModule = authenticationModule;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {

        final String key = update.getUpdatedObject().getTypeAttribute().getCleanValue().toString();
        final int lastColon = key.lastIndexOf(':');
        if (lastColon < 0) {
            return Collections.emptyList();
        }
        final CIString parentKey = ciString(key.substring(0, lastColon));

        final RpslObject parent = CollectionHelper.uniqueResult(
                objectDao.getByKeys(findObjectType(update.getType(), parentKey), Collections.singleton(parentKey)));

        if (parent == null) {
            return Arrays.asList(UpdateMessages.parentObjectNotFound(parentKey));
        }

        final List<RpslObject> referencedMaintainers = findMaintainers(parent);
        final List<RpslObject> authenticatedMaintainers = authenticationModule.authenticate(update, updateContext, referencedMaintainers, MntByAuthentication.class);

        if (authenticatedMaintainers.isEmpty()) {
            return Arrays.asList(UpdateMessages.parentAuthenticationFailed(parent, findAttributeType(parent), referencedMaintainers));
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

    ObjectType findObjectType(final ObjectType objectType, final CIString value) {
        if (AttributeType.AUT_NUM.isValidValue(objectType, value)) {
            return ObjectType.AUT_NUM;
        }

        if (AttributeType.AS_SET.isValidValue(objectType, value)) {
            return ObjectType.AS_SET;
        }

        return objectType;
    }

    private AttributeType findAttributeType(final RpslObject object) {
        final Set<CIString> maintainers = object.getValuesForAttribute(AttributeType.MNT_LOWER);
        return maintainers.isEmpty() ? AttributeType.MNT_BY : AttributeType.MNT_LOWER;
    }

    private List<RpslObject> findMaintainers(final RpslObject object) {
        Set<CIString> maintainers = object.getValuesForAttribute(AttributeType.MNT_LOWER);
        if (maintainers.isEmpty()) {
            maintainers = object.getValuesForAttribute(AttributeType.MNT_BY);
        }

        return objectDao.getByKeys(ObjectType.MNTNER, maintainers);
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
