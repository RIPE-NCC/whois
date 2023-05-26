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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class ReferenceAuthentication extends AuthenticationStrategyBase {
    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;

    public ReferenceAuthentication(AuthenticationModule credentialValidators, RpslObjectDao rpslObjectDao) {
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
    }

    List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext,
                                         final Map<RpslObject, List<RpslObject>> candidatesMap) {
        final List<Message> authenticationMessages = Lists.newArrayList();

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

    Map<RpslObject, List<RpslObject>> getCandidates(final PreparedUpdate update, final UpdateContext updateContext,
                                                    final ObjectType objectType,
                                                    final List<AttributeType> attributeType) {
        final Map<RpslObject, List<RpslObject>> candidates = new LinkedHashMap<>();

        final Collection<CIString> newReferences = attributeType.stream().flatMap(ref -> update.getNewValues(ref).stream()).toList();
        final List<RpslObject> rpslObjects = rpslObjectDao.getByKeys(objectType, newReferences);
        if (isSelfReference(update, newReferences, objectType)) {
            rpslObjects.add(update.getUpdatedObject());
        }

        for (final RpslObject rpslObject : rpslObjects) {
            final List<RpslObject> maintainers = Lists.newArrayList();

            for (final CIString mntRef : rpslObject.getValuesForAttribute(AttributeType.MNT_REF)) {
                try {
                    maintainers.add(rpslObjectDao.getByKey(ObjectType.MNTNER, mntRef.toString()));
                } catch (EmptyResultDataAccessException e) {
                    updateContext.addMessage(update, UpdateMessages.nonexistantMntRef(rpslObject.getKey(), mntRef));
                }
            }

            //No candidates if mnt-ref doesn't exist and is not required attribute (req just in organisation objects)
            if (ObjectType.ORGANISATION.equals(objectType) || !maintainers.isEmpty()){
                candidates.put(rpslObject, maintainers);
            }
        }

        return candidates;
    }
    private boolean isSelfReference(final PreparedUpdate update, final Collection<CIString> newReferences, final ObjectType objectType) {
        return update.getType().equals(objectType) && newReferences.contains(update.getUpdatedObject().getKey());
    }
}
