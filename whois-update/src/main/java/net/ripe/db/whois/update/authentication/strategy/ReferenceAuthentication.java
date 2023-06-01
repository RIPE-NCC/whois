package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ReferenceAuthentication extends AuthenticationStrategyBase {
    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;

    private static final List<AttributeType> REFERENCED_ATTRIBUTE_TYPES = List.of(
            AttributeType.ABUSE_C, AttributeType.ADMIN_C,
            AttributeType.ZONE_C, AttributeType.TECH_C,
            AttributeType.AUTHOR, AttributeType.PING_HDL,
            AttributeType.MNT_IRT, AttributeType.MNT_BY,
            AttributeType.MNT_DOMAINS, AttributeType.MNT_ROUTES,
            AttributeType.MNT_LOWER, AttributeType.MNT_REF,
            AttributeType.ORG);

    public ReferenceAuthentication(final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao) {
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public boolean supports(PreparedUpdate update) {
        return REFERENCED_ATTRIBUTE_TYPES.stream().map(update::getNewValues).anyMatch(values -> !values.isEmpty());
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> authenticationMessages = Lists.newArrayList();
        final Map<RpslObject, List<RpslObject>> candidatesMap = getCandidates(update, updateContext);
        final Set<RpslObject> authenticatedObjects = Sets.newHashSet();

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

    private Map<RpslObject, List<RpslObject>> getCandidates(final PreparedUpdate update, final UpdateContext updateContext) {
        final Map<RpslObject, List<RpslObject>> candidates = Maps.newHashMap();

        for (final RpslObject rpslObject :  getReferencedObjects(update)) {
            final List<RpslObject> maintainers = Lists.newArrayList();

            for (final CIString mntRef : rpslObject.getValuesForAttribute(AttributeType.MNT_REF)) {
                try {
                    maintainers.add(rpslObjectDao.getByKey(ObjectType.MNTNER, mntRef.toString()));
                } catch (EmptyResultDataAccessException e) {
                    updateContext.addMessage(update, UpdateMessages.nonexistantMntRef(rpslObject.getKey(), mntRef));
                }
            }

            // if the mnt-ref is required in the referenced object, ej: Organisation object. We should throw error
            if (!maintainers.isEmpty() || ObjectTemplate.getTemplate(rpslObject.getType()).getMandatoryAttributes().contains(AttributeType.MNT_REF)){
                candidates.put(rpslObject, maintainers);
            }
        }

        return candidates;
    }

    private List<RpslObject> getAllObjects(final Set<ObjectType> objectsTypes, final Set<CIString> newOrgReferences) {
        final List<RpslObject> objects = Lists.newArrayList();
        objectsTypes.forEach( objectType -> objects.addAll(rpslObjectDao.getByKeys(objectType, newOrgReferences)));
        return objects;
    }

    private boolean isSelfReference(final RpslObject updatedObject, final Collection<CIString> newReferences, final Set<ObjectType> objectTypes) {
        return objectTypes.contains(updatedObject.getType()) && newReferences.contains(updatedObject.getKey());
    }

    private  List<RpslObject> getReferencedObjects(final PreparedUpdate update) {
        final List<RpslObject> referenceObjects = Lists.newArrayList();

        for (final AttributeType attributeType : REFERENCED_ATTRIBUTE_TYPES) {
            final Set<CIString> attributeValues = update.getNewValues(attributeType);
            if (attributeValues.isEmpty()) {
                continue;
            }
            referenceObjects.addAll(getAllObjects(attributeType.getReferences(), attributeValues));

            if (isSelfReference(update.getUpdatedObject(), attributeValues, attributeType.getReferences())) {
                referenceObjects.add(update.getUpdatedObject());
            }
        }

        return referenceObjects;
    }
}

