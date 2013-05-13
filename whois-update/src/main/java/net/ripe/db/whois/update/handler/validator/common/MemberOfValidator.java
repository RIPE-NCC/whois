package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MemberOfValidator implements BusinessRuleValidator {
    private final RpslObjectDao objectDao;
    private final Map<ObjectType, ObjectType> objectTypeMap;
    private static final CIString ANY = CIString.ciString("ANY");

    @Autowired
    public MemberOfValidator(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;

        this.objectTypeMap = Maps.newHashMapWithExpectedSize(3);
        objectTypeMap.put(ObjectType.AUT_NUM, ObjectType.AS_SET);
        objectTypeMap.put(ObjectType.ROUTE, ObjectType.ROUTE_SET);
        objectTypeMap.put(ObjectType.ROUTE6, ObjectType.ROUTE_SET);
        objectTypeMap.put(ObjectType.INET_RTR, ObjectType.RTR_SET);
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.AUT_NUM, ObjectType.ROUTE, ObjectType.ROUTE6, ObjectType.INET_RTR);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Collection<CIString> memberOfs = update.getUpdatedObject().getValuesForAttribute((AttributeType.MEMBER_OF));
        final Set<CIString> updatedObjectMaintainers = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
        if (memberOfs.isEmpty()) {
            return;
        }

        final ObjectType referencedObjectType = objectTypeMap.get(update.getType());
        final Set<CIString> unsupportedSets = findUnsupportedMembers(memberOfs, updatedObjectMaintainers, referencedObjectType);
        if (!unsupportedSets.isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.membersNotSupportedInReferencedSet(unsupportedSets.toString()));
        }
    }

    private Set<CIString> findUnsupportedMembers(final Collection<CIString> memberOfs, final Set<CIString> originalObjectMaintainers, final ObjectType objectType) {
        final Set<CIString> unsupportedMembers = Sets.newLinkedHashSet();
        for (final CIString memberOf : memberOfs) {
            try {
                final RpslObject referencedSet = objectDao.getByKey(objectType, memberOf.toString());
                if (isInvalidMember(originalObjectMaintainers, referencedSet.getValuesForAttribute(AttributeType.MBRS_BY_REF))) {
                    unsupportedMembers.add(memberOf);
                }
            } catch (EmptyResultDataAccessException e) {
                //Error Message already added by the ReferencedObjectsExistValidator.
            }
        }
        return unsupportedMembers;
    }

    private boolean isInvalidMember(final Set<CIString> originalObjectMaintainers, final Set<CIString> referencedMaintainers) {
        if(referencedMaintainers.contains(ANY))  {
            return false;
        }
        Sets.SetView<CIString> difference = Sets.difference(originalObjectMaintainers, referencedMaintainers);
        return difference.size() >= originalObjectMaintainers.size();
    }
}
