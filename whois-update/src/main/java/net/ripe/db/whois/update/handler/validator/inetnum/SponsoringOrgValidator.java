package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_BY;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_TYPE;
import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;
import static net.ripe.db.whois.update.domain.UpdateMessages.sponsoringOrgChanged;
import static net.ripe.db.whois.update.domain.UpdateMessages.sponsoringOrgNotLIR;

@Component
public class SponsoringOrgValidator implements BusinessRuleValidator {
    private static final List<Action> ACTIONS = ImmutableList.of(CREATE, MODIFY);
    private static final List<ObjectType> OBJECT_TYPES = ImmutableList.of(INETNUM, INET6NUM, AUT_NUM);

    private final Maintainers maintainers;
    private final RpslObjectDao objectDao;

    @Autowired
    public SponsoringOrgValidator(final Maintainers maintainers,
                                  final RpslObjectDao objectDao) {
        this.maintainers = maintainers;
        this.objectDao = objectDao;
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
        final boolean sponsoringOrgHasChanged = sponsoringOrgHasChanged(update);
        if (!sponsoringOrgHasChanged) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        if (updatedObject.containsAttribute(SPONSORING_ORG)) {
            final List<RpslObject> sponsoringOrganisations = objectDao.getByKeys(ORGANISATION, Collections.singletonList(updatedObject.getValueForAttribute(SPONSORING_ORG)));

            if (sponsoringOrganisations.isEmpty() || !sponsoringOrganisations.get(0).getValueForAttribute(ORG_TYPE).equals("LIR")) {
                updateContext.addMessage(update, sponsoringOrgNotLIR());
            }
        }

        final boolean hasRsMaintainer = !Sets.intersection(maintainers.getRsMaintainers(), updatedObject.getValuesForAttribute(MNT_BY)).isEmpty();
        final boolean isOverride =  updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);

        if (!hasRsMaintainer && !isOverride) {
            updateContext.addMessage(update, sponsoringOrgChanged());
        }
    }

    private boolean sponsoringOrgHasChanged(final PreparedUpdate update) {
        final CIString refSponsoringOrg = update.getReferenceObject().getValueOrNullForAttribute(SPONSORING_ORG);
        final CIString updSponsoringOrg = update.getUpdatedObject().getValueOrNullForAttribute(SPONSORING_ORG);
        final boolean presentOnCreate = update.getAction() == CREATE && (refSponsoringOrg != null && !refSponsoringOrg.equals(""));

        return presentOnCreate || (update.getAction() == MODIFY && !Objects.equal(refSponsoringOrg, updSponsoringOrg));
    }
}
