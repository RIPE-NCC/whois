package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

@Component
public class ReferenceCheck implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final RpslObjectDao rpslObjectDao;
    private final ReferencesDao referencesDao;

    @Autowired
    public ReferenceCheck(final RpslObjectDao rpslObjectDao, final ReferencesDao referencesDao) {
        this.rpslObjectDao = rpslObjectDao;
        this.referencesDao = referencesDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final InetStatus inetStatus = getStatus(update);
        final List<RpslAttribute> updatedOrgAttributes = update.getUpdatedObject().findAttributes(AttributeType.ORG);

        if (inetStatus.needsOrgReference() && updatedOrgAttributes.isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.orgAttributeMissing());
            return;
        }

        if (updatedOrgAttributes.isEmpty()) {
            return;
        }

        final RpslAttribute org = updatedOrgAttributes.get(0);
        final RpslObject referencedOrganisation = findOrgReference(org);
        if (referencedOrganisation == null) {
            updateContext.addMessage(update, UpdateMessages.referenceNotFound(org.getCleanValue()));
            return;
        }

        final CIString cleanOrgTypeValue = referencedOrganisation.findAttribute(AttributeType.ORG_TYPE).getCleanValue();
        final OrgType orgType = OrgType.getFor(cleanOrgTypeValue);
        if (orgType == null || !inetStatus.isValidOrgType(orgType)) {
            updateContext.addMessage(update, UpdateMessages.wrongOrgType(inetStatus.getAllowedOrgTypes()));
        }
    }

    private RpslObject findOrgReference(final RpslAttribute org) {
        final RpslObjectInfo referencedOrganisationInfo = referencesDao.getAttributeReference(org.getType(), org.getCleanValue());

        return (referencedOrganisationInfo == null ? null : rpslObjectDao.getByKey(ObjectType.ORGANISATION, referencedOrganisationInfo.getKey()));
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
