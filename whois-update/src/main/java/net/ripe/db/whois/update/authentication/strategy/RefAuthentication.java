package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RefAuthentication extends AuthenticationStrategyBase {

    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;

    private static final List<AttributeType> REFERENCED_OBJECT_TYPES = List.of(AttributeType.ABUSE_C,
            AttributeType.ADMIN_C,
            AttributeType.ZONE_C, AttributeType.TECH_C, AttributeType.AUTHOR, AttributeType.PING_HDL,
            AttributeType.MNT_IRT,
            AttributeType.MNT_BY, AttributeType.MNT_DOMAINS, AttributeType.MNT_ROUTES, AttributeType.MNT_LOWER,
            AttributeType.MNT_REF, AttributeType.MNT_NFY, AttributeType.ORG);

    public RefAuthentication(final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao) {
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
    }


    Map<RpslObject, List<RpslObject>> getCandidates(final PreparedUpdate update, final UpdateContext updateContext) {
        final Map<RpslObject, List<RpslObject>> candidates = new LinkedHashMap<>();

        for (final AttributeType attributeType : REFERENCED_OBJECT_TYPES) {
            final Collection<CIString> attributeValues = update.getNewValues(attributeType);
            if (attributeValues.isEmpty()){
                continue;
            }
            final List<RpslObject> referenceObjects = getAllObjects(attributeType.getReferences(), attributeValues);

            if (isSelfReference(update, attributeValues, attributeType.getReferences())) {
                referenceObjects.add(update.getUpdatedObject());
            }

            for (final RpslObject rpslObject : referenceObjects) {
                final List<RpslObject> maintainers = Lists.newArrayList();

                for (final CIString mntRef : rpslObject.getValuesForAttribute(AttributeType.MNT_REF)) {
                    try {
                        maintainers.add(rpslObjectDao.getByKey(ObjectType.MNTNER, mntRef.toString()));
                    } catch (EmptyResultDataAccessException e) {
                        updateContext.addMessage(update, UpdateMessages.nonexistantMntRef(rpslObject.getKey(), mntRef));
                    }
                }

                if (attributeType.getReferences().contains(ObjectType.ORGANISATION) || !maintainers.isEmpty()){
                    candidates.put(rpslObject, maintainers);
                }
            }
        }
        return candidates;
    }

    private List<RpslObject> getAllObjects(final Set<ObjectType> objectsTypes, final Collection<CIString> newOrgReferences) {
        final List<RpslObject> objects = Lists.newArrayList();
        objectsTypes.forEach( objectType -> objects.addAll(rpslObjectDao.getByKeys(objectType, newOrgReferences)));
        return objects;
    }
    private boolean isSelfReference(final PreparedUpdate update, final Collection<CIString> newReferences,
                                    final Set<ObjectType> objectTypes) {
        return objectTypes.contains(update.getType()) && newReferences.contains(update.getUpdatedObject().getKey());
    }

    @Override
    public boolean supports(PreparedUpdate update) {
        return REFERENCED_OBJECT_TYPES.stream().map(update::getNewValues).anyMatch(values -> !values.isEmpty());
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) throws AuthenticationFailedException {
        final List<Message> authenticationMessages = Lists.newArrayList();
        final Map<RpslObject, List<RpslObject>> candidatesMap = getCandidates(update, updateContext);
        final Set<RpslObject> authenticatedObjects = Sets.newLinkedHashSet();
        for (final Map.Entry<RpslObject, List<RpslObject>> candidatesEntry : candidatesMap.entrySet()) {
            final List<RpslObject> candidates = candidatesEntry.getValue();
            final List<RpslObject> authenticatedBy = credentialValidators.authenticate(update, updateContext, candidates, getClass());

            if (authenticatedBy.isEmpty()) {
                final RpslObject candidate = candidatesEntry.getKey();
                authenticationMessages.add(UpdateMessages.authenticationFailed(candidate, AttributeType.MNT_REF, candidates));
            } else {
                authenticatedObjects.addAll(authenticatedBy);
            }
        }

        if (!authenticationMessages.isEmpty()) {
            throw new AuthenticationFailedException(authenticationMessages, Sets.newLinkedHashSet(Iterables.concat(candidatesMap.values())));
        }

        return Lists.newArrayList(authenticatedObjects);
    }
}
