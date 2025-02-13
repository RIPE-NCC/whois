package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class MemberByRefValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.AS_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET, ObjectType.PEERING_SET);

    private static final CIString ANY = CIString.ciString("ANY");

    private final RpslObjectDao objectDao;

    @Autowired
    public MemberByRefValidator(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<CIString> membersByRef = update.getUpdatedObject().getValuesForAttribute((AttributeType.MBRS_BY_REF));
        if (membersByRef.isEmpty() || membersByRef.contains(ANY) || !hasMembersByRefChanged(update)) {
            return Collections.emptyList();
        }

        final List<RpslObjectInfo> incomingReferences = objectDao.findByAttribute( AttributeType.MEMBER_OF, update.getUpdatedObject().getKey().toString());

        final Set<String> unsupportedSets = findUnsupportedMembers(incomingReferences, membersByRef);
        if (!unsupportedSets.isEmpty()) {
            //TODO: change error message
            return Arrays.asList(UpdateMessages.membersByRefChangedInSet(unsupportedSets));
        }

        return Collections.emptyList();
    }

    private boolean hasMembersByRefChanged(final PreparedUpdate update) {
        return !update.getDifferences(AttributeType.MBRS_BY_REF).isEmpty();
    }

    private Set<String> findUnsupportedMembers(final Collection<RpslObjectInfo> incomingReferences, final Set<CIString> membersByRef) {
        final Set<String> unsupportedMembers = Sets.newLinkedHashSet();
        for (final RpslObjectInfo incomingRefs : incomingReferences) {
            try {
                final RpslObject refResource = objectDao.getByKey(incomingRefs.getObjectType(), incomingRefs.getKey());
                if (!isValidMember(membersByRef, refResource.getValuesForAttribute(AttributeType.MNT_BY))) {
                    unsupportedMembers.add(incomingRefs.getKey());
                }
            } catch (EmptyResultDataAccessException e) {
                //Error Message already added by the ReferencedObjectsExistValidator.
            }
        }
        return unsupportedMembers;
    }

    private boolean isValidMember(final Set<CIString> originalObjectMaintainers, final Set<CIString> referencedMaintainers) {
        return referencedMaintainers.stream().anyMatch(originalObjectMaintainers::contains);
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
