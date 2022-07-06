package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class OrgNameAndCountryAttrValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final Set<ObjectType> RESOURCE_OBJECT_TYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM);

    private final RpslObjectUpdateDao objectUpdateDao;
    private final RpslObjectDao objectDao;
    private final Maintainers maintainers;

    @Autowired
    public OrgNameAndCountryAttrValidator(final RpslObjectUpdateDao objectUpdateDao, final RpslObjectDao objectDao, final Maintainers maintainers) {
        this.objectUpdateDao = objectUpdateDao;
        this.objectDao = objectDao;
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject originalObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        if (isLir(originalObject)) {
            // See: LirRipeMaintainedAttributesValidator
            return;
        }

        final boolean isOrgNameModified = isAttributeModified(ORG_NAME, originalObject, updatedObject);
        final boolean isCountryCodeModified = isAttributeModified(COUNTRY, originalObject, updatedObject);
        if (!isOrgNameModified && !isCountryCodeModified) {
            return;
        }

        final Subject subject = updateContext.getSubject(update);
        if (alreadyHasAllPossibleAuthorisations(subject)) {
            return;
        }

        if (isReferencedByRsMaintainedResource(originalObject)) {
            if(isOrgNameModified) {
                updateContext.addMessage(update, updatedObject.findAttribute(ORG_NAME), UpdateMessages.cantChangeOrgName());
            }

            if(isCountryCodeModified) {
                updateContext.addMessage(update, updatedObject.findAttribute(COUNTRY), UpdateMessages.cantChangeCountryCode());
            }
        }
    }

    private boolean isLir(final RpslObject organisation) {
        return OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
    }

    private boolean isReferencedByRsMaintainedResource(final RpslObject rpslObject) {
        for (RpslObjectInfo referencedObjectInfo : objectUpdateDao.getReferences(rpslObject)) {
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
        final CIString originalAttrValue = originalObject.getValueOrNullForAttribute(attrType);
        final CIString updatedAttrValue = updatedObject.getValueOrNullForAttribute(attrType);

        return (originalAttrValue != null) &&
                (updatedAttrValue != null) &&
                !(Objects.equals(originalAttrValue.toString(), updatedAttrValue.toString()));
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
