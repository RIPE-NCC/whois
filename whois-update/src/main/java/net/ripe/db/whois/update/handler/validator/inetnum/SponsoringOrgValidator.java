package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

        if (action == Action.CREATE && !updatedObject.containsAttribute(SPONSORING_ORG) && updatedObject.containsAttribute(AttributeType.ORG) && hasEndUserMntner(updatedObject)) {
            final List<RpslObject> organisations = objectDao.getByKeys(ObjectType.ORGANISATION, Collections.singleton(updatedObject.getValueForAttribute(AttributeType.ORG)));
            if (!organisations.isEmpty()) {
                if (OrgType.OTHER == OrgType.getFor(organisations.get(0).getValueForAttribute(AttributeType.ORG_TYPE))) {
                    updateContext.addMessage(update, UpdateMessages.sponsoringOrgMustBePresent());
                    return;
                }
            }
        }

        final boolean sponsoringOrgHasChanged = sponsoringOrgHasChangedAtAll(refSponsoringOrg, updSponsoringOrg, action);
        if (!sponsoringOrgHasChanged) {
            return;
        }

        if (updatedObject.containsAttribute(SPONSORING_ORG)) {
            final List<RpslObject> sponsoringOrganisations = objectDao.getByKeys(ORGANISATION, Collections.singletonList(updatedObject.getValueOrNullForAttribute(SPONSORING_ORG)));

            if (sponsoringOrganisations.isEmpty() || !sponsoringOrganisations.get(0).getValueForAttribute(ORG_TYPE).equals("LIR")) {
                updateContext.addMessage(update, sponsoringOrgNotLIR());
            }
        }

        final boolean authByRS =  updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);
        final boolean isOverride =  updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);

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

    private boolean sponsoringOrgHasChangedAtAll(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        final boolean presentOnCreate = action == CREATE && (referencedSponsoringOrg != null && !referencedSponsoringOrg.equals(""));
        return presentOnCreate || (action == MODIFY && !Objects.equal(referencedSponsoringOrg, updatedSponsoringOrg));
    }

    private boolean sponsoringOrgAdded(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        final boolean hasReferenced = referencedSponsoringOrg != null && !referencedSponsoringOrg.equals("");
        return action == CREATE && hasReferenced || (action == MODIFY && (!hasReferenced && updatedSponsoringOrg != null && !updatedSponsoringOrg.equals("")));
    }

    private boolean sponsoringOrgRemoved(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        return action == MODIFY && (referencedSponsoringOrg != null && !referencedSponsoringOrg.equals("") && updatedSponsoringOrg == null);
    }

    private boolean sponsoringOrgChanged(final CIString referencedSponsoringOrg, final CIString updatedSponsoringOrg, final Action action) {
        final boolean referencedPresent = referencedSponsoringOrg != null && !referencedSponsoringOrg.equals("");
        final boolean updatedPresent = updatedSponsoringOrg != null && !updatedSponsoringOrg.equals("");
        return action == MODIFY && referencedPresent && updatedPresent && !Objects.equal(referencedSponsoringOrg, updatedSponsoringOrg);
    }

    private boolean hasEndUserMntner(final RpslObject object) {
        final Set<CIString> mntBy = object.getValuesForAttribute(AttributeType.MNT_BY);
        return !Sets.intersection(maintainers.getEnduserMaintainers(), mntBy).isEmpty();
    }
}
