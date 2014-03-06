package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class OrgNameNotChangedValidator implements BusinessRuleValidator {
    private static final Set<ObjectType> OBJECT_TYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM);

    private final RpslObjectUpdateDao objectUpdateDao;
    private final RpslObjectDao objectDao;
    private final Maintainers maintainers;

    @Autowired
    public OrgNameNotChangedValidator(final RpslObjectUpdateDao objectUpdateDao, final RpslObjectDao objectDao, final Maintainers maintainers) {
        this.objectUpdateDao = objectUpdateDao;
        this.objectDao = objectDao;
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Collections.singletonList(Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Collections.singletonList(ObjectType.ORGANISATION);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject original = update.getReferenceObject();
        final CIString storedOrgName = original.getValueOrNullForAttribute(AttributeType.ORG_NAME);
        final CIString updateOrgName = update.getUpdatedObject().getValueOrNullForAttribute(AttributeType.ORG_NAME);

        if (Objects.equals(storedOrgName, updateOrgName)) {
            return;
        }

        final Set<RpslObjectInfo> references = objectUpdateDao.getReferences(original);
        boolean rsMaintainedReferenceFound = false;

        for (RpslObjectInfo info : references) {
            if (OBJECT_TYPES.contains(info.getObjectType())) {
                final RpslObject resource = objectDao.getById(info.getObjectId());
                if (isMaintainedByRs(resource)) {
                    rsMaintainedReferenceFound = true;
                    break;
                }
            }
        }

        final Subject subject = updateContext.getSubject(update);
        if ( rsMaintainedReferenceFound && !(update.isOverride() || subject.hasPrincipal(Principal.RS_MAINTAINER))) {
            updateContext.addMessage(update, UpdateMessages.cantChangeOrgName());
        }
    }

    private boolean isMaintainedByRs(final RpslObject resourceObject) {
        final Set<CIString> objectMaintainers = resourceObject.getValuesForAttribute(AttributeType.MNT_BY);
        return !Sets.intersection(this.maintainers.getRsMaintainers(), objectMaintainers).isEmpty();
    }
}
