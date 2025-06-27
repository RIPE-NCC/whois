package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;

@Component
public class OrgNameAndCountryAttrValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final Set<ObjectType> RESOURCE_OBJECT_TYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM);

    private final RpslObjectDao objectDao;
    private final Maintainers maintainers;
    private final ReferencesDao referencesDao;

    @Autowired
    public OrgNameAndCountryAttrValidator(final RpslObjectDao objectDao, final Maintainers maintainers, final ReferencesDao referencesDao) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
        this.referencesDao = referencesDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject originalObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        if (isLir(originalObject)) {
            // See: LirRipeMaintainedAttributesValidator
            return Collections.emptyList();
        }

        final boolean isOrgNameModified = isAttributeModified(ORG_NAME, originalObject, updatedObject);
        final boolean isCountryCodeModified = isAttributeModified(COUNTRY, originalObject, updatedObject);
        if (!isOrgNameModified && !isCountryCodeModified) {
            return Collections.emptyList();
        }

        final Subject subject = updateContext.getSubject(update);
        if (alreadyHasAllPossibleAuthorisations(subject)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        if (isReferencedByRsMaintainedResource(originalObject)) {
            if(isOrgNameModified) {
                messages.add(UpdateMessages.canOnlyBeChangedByRipeNCC(updatedObject.findAttribute(ORG_NAME)));
            }

            if(isCountryCodeModified) {
                messages.add(UpdateMessages.canOnlyBeChangedByRipeNCC(COUNTRY));
            }
        }

        return messages;
    }

    private boolean isLir(final RpslObject organisation) {
        return OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
    }

    private boolean isReferencedByRsMaintainedResource(final RpslObject rpslObject) {
        for (RpslObjectInfo referencedObjectInfo : referencesDao.getReferences(rpslObject)) {
            if (RESOURCE_OBJECT_TYPES.contains(referencedObjectInfo.getObjectType())) {
                final RpslObject referencedObject = objectDao.getById(referencedObjectInfo.getObjectId());
                if (isMaintainedByRs(referencedObject)) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isAttributeModified(final AttributeType attrType, final RpslObject originalObject, final RpslObject updatedObject) {
        final String originalAttrValue = originalObject.containsAttribute(attrType) ? originalObject.getValueForAttribute(attrType).toString() : null;
        final String updatedAttrValue = updatedObject.containsAttribute(attrType) ? updatedObject.getValueForAttribute(attrType).toString() : null;

        return !StringUtils.equals(originalAttrValue, updatedAttrValue);
    }

    private boolean alreadyHasAllPossibleAuthorisations(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.RS_MAINTAINER);
    }

    private boolean isMaintainedByRs(final RpslObject rpslObject) {
        final Set<CIString> objectMaintainers = rpslObject.getValuesForAttribute(AttributeType.MNT_BY);
        return maintainers.isRsMaintainer(objectMaintainers);
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
