package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MaintainedReferencedMaintainerPersonRolesValidator extends AbstractObjectIsMaintainedValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Autowired
    public MaintainedReferencedMaintainerPersonRolesValidator(final RpslObjectDao rpslObjectDao) {
        super(rpslObjectDao);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        boolean nonexistantMntner = false;

        for (final CIString value : updatedObject.getValuesForAttribute(AttributeType.MNT_BY)) {
            if (isSelfReference(updatedObject, value)) {
                continue;
            }

            try {
                final RpslObject object = rpslObjectDao.getByKey(ObjectType.MNTNER, value.toString());
                for (RpslObject rpslObject : validateReferencedPersonsAndRoles(object)) {
                    updateContext.addMessage(update, UpdateMessages.referencedObjectMissingAttribute(rpslObject.getType(), rpslObject.getKey(), ObjectType.MNTNER, value, AttributeType.MNT_BY));
                }
            } catch (EmptyResultDataAccessException e) {
                updateContext.addMessage(update, UpdateMessages.maintainerNotFound(value));
                nonexistantMntner = true;
            }
        }

        if (nonexistantMntner && !hasReferenceToPersonRole(updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.createFirstPersonMntnerForOrganisation());
        }
    }

    private boolean isSelfReference(final RpslObject updatedObject, final CIString value) {
        return ObjectType.MNTNER.equals(updatedObject.getType()) && updatedObject.getKey().equals(value);
    }

    private boolean hasReferenceToPersonRole(final RpslObject object) {
        final List<RpslAttribute> attributes = object.findAttributes(AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.PING_HDL, AttributeType.AUTHOR, AttributeType.ABUSE_C);
        final Set<CIString> attrValues = Sets.newHashSet();
        for (RpslAttribute attr : attributes) {
            attrValues.addAll(attr.getReferenceValues());
        }

        if (rpslObjectDao.getByKeys(ObjectType.PERSON, attrValues).isEmpty()) {
            if (rpslObjectDao.getByKeys(ObjectType.ROLE, attrValues).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
