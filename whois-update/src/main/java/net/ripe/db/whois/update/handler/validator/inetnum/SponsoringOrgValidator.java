package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_BY;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_TYPE;
import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;
import static net.ripe.db.whois.common.rpsl.ObjectType.*;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class SponsoringOrgValidator implements BusinessRuleValidator {
    private final Maintainers maintainers;
    private final RpslObjectDao objectDao;

    @Autowired
    public SponsoringOrgValidator(final Maintainers maintainers, final RpslObjectDao objectDao) {
        this.maintainers = maintainers;
        this.objectDao = objectDao;
    }

    @Override
    public List<Action> getActions() {
        return newArrayList(CREATE, MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return newArrayList(INETNUM, INET6NUM, AUT_NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final boolean sponsoringOrgHasChanged = sponsoringOrgHasChanged(update);
        if (!sponsoringOrgHasChanged) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        if (updatedObject.containsAttribute(SPONSORING_ORG)) {
            final RpslObject sponsoringOrganisation = objectDao.getByKey(ORGANISATION, updatedObject.getValueForAttribute(SPONSORING_ORG));

            if (!sponsoringOrganisation.getValueForAttribute(ORG_TYPE).equals("LIR")) {
                updateContext.addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
            }
        }

        final boolean hasRsMaintainer = !Sets.intersection(
                maintainers.getRsMaintainers(),
                updatedObject.getValuesForAttribute(MNT_BY)).isEmpty();

        if (!hasRsMaintainer && !update.isOverride()) {
            updateContext.addMessage(update, UpdateMessages.sponsoringOrgChanged());
        }
    }

    private boolean sponsoringOrgHasChanged(final PreparedUpdate update) {
        final CIString refSponsoringOrg = update.getReferenceObject().getValueOrNullForAttribute(SPONSORING_ORG);
        final CIString updSponsoringOrg = update.getUpdatedObject().getValueOrNullForAttribute(SPONSORING_ORG);
        final boolean presentOnCreate = update.getAction() == CREATE && (refSponsoringOrg != null && !refSponsoringOrg.equals(""));

        return presentOnCreate || (update.getAction() == MODIFY && !Objects.equal(refSponsoringOrg, updSponsoringOrg));
    }
}
