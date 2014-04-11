package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
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
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.ORG;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_TYPE;
import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;
import static net.ripe.db.whois.update.domain.UpdateMessages.sponsoringOrgNotLIR;

@Component
public class SponsoringOrgValidator implements BusinessRuleValidator {
    private static final List<Action> ACTIONS = ImmutableList.of(CREATE, MODIFY);
    private static final List<ObjectType> OBJECT_TYPES = ImmutableList.of(INETNUM, INET6NUM, AUT_NUM);
    private static final Set<? extends InetStatus> ALLOWED_STATUSES = ImmutableSet.of(InetnumStatus.ASSIGNED_PI, InetnumStatus.ASSIGNED_ANYCAST, Inet6numStatus.ASSIGNED_PI);

    private final RpslObjectDao objectDao;
    private final Maintainers maintainers;

    @Autowired
    public SponsoringOrgValidator(final RpslObjectDao objectDao, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public List<ObjectType> getTypes() {
        return OBJECT_TYPES;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString refSponsoringOrg = update.getReferenceObject().getValueOrNullForAttribute(SPONSORING_ORG);
        final CIString updSponsoringOrg = update.getUpdatedObject().getValueOrNullForAttribute(SPONSORING_ORG);

        final Action action = update.getAction();
        final RpslObject updatedObject = update.getUpdatedObject();

        if (sponsoringOrgStatusCheck(updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.sponsoringOrgNotAllowedWithStatus(updatedObject.getValueForAttribute(AttributeType.STATUS)));
            return;
        }

        if (sponsoringOrgMustBePresent(action, updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.sponsoringOrgMustBePresent());
            return;
        }

        if (!sponsoringOrgHasChangedAtAll(refSponsoringOrg, updSponsoringOrg, action)) {
            return;
        }

        if (updSponsoringOrg != null) {
            final RpslObject sponsoringOrganisation = objectDao.getByKeyOrNull(ORGANISATION, updSponsoringOrg);

            if (sponsoringOrganisation != null && !sponsoringOrganisation.getValueForAttribute(ORG_TYPE).equals("LIR")) {
                updateContext.addMessage(update, updatedObject.findAttribute(AttributeType.SPONSORING_ORG), sponsoringOrgNotLIR());
            }
        }

        final boolean authByRS = updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);
        final boolean isOverride = updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);

        if (!authByRS && !isOverride) {
            if (sponsoringOrgAdded(refSponsoringOrg, updSponsoringOrg, action)) {
                updateContext.addMessage(update, UpdateMessages.sponsoringOrgAdded());
            } else if (sponsoringOrgRemoved(refSponsoringOrg, updSponsoringOrg, action)) {
                updateContext.addMessage(update, UpdateMessages.sponsoringOrgRemoved());
            } else if (sponsoringOrgChanged(refSponsoringOrg, updSponsoringOrg, action)) {
                updateContext.addMessage(update, UpdateMessages.sponsoringOrgChanged());
            }
        }
    }

    private boolean sponsoringOrgStatusCheck(RpslObject updatedObject) {
        final CIString statusString = updatedObject.getValueForAttribute(AttributeType.STATUS);
        InetStatus status;

        try {
            switch (updatedObject.getType()) {
                case INETNUM:
                    status = InetnumStatus.getStatusFor(statusString);
                    break;

                case INET6NUM:
                    status = Inet6numStatus.getStatusFor(statusString);
                    break;

                default:
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        return !ALLOWED_STATUSES.contains(status);
    }

    private boolean sponsoringOrgMustBePresent(final Action action, final RpslObject updatedObject) {
        if (action == CREATE && !updatedObject.containsAttribute(SPONSORING_ORG) && updatedObject.containsAttribute(ORG) && hasEndUserMntner(updatedObject)) {
            final RpslObject organisation = objectDao.getByKeyOrNull(ObjectType.ORGANISATION, updatedObject.getValueForAttribute(ORG));
            if (organisation != null && OrgType.OTHER == OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE))) {
                return true;
            }
        }
        return false;
    }

    private boolean sponsoringOrgHasChangedAtAll(final CIString referenceSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        return action == CREATE && !CIString.isBlank(referenceSponsoringOrg)
                || (action == MODIFY && !Objects.equal(referenceSponsoringOrg, updatedSponsoringOrg));
    }

    private boolean sponsoringOrgAdded(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        final boolean existingSponsoringOrg = CIString.isBlank(referencedSponsoringOrg);
        return (action == CREATE && !existingSponsoringOrg)
                || (action == MODIFY && existingSponsoringOrg && !CIString.isBlank(updatedSponsoringOrg));
    }

    private boolean sponsoringOrgRemoved(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        return action == MODIFY && !CIString.isBlank(referencedSponsoringOrg) && CIString.isBlank(updatedSponsoringOrg);
    }

    private boolean sponsoringOrgChanged(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        return action == MODIFY && !CIString.isBlank(referencedSponsoringOrg) && !CIString.isBlank(updatedSponsoringOrg)
                && !Objects.equal(referencedSponsoringOrg, updatedSponsoringOrg);
    }

    private boolean hasEndUserMntner(final RpslObject object) {
        final Set<CIString> mntBy = object.getValuesForAttribute(AttributeType.MNT_BY);
        return !Sets.intersection(maintainers.getEnduserMaintainers(), mntBy).isEmpty();
    }
}
