package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class OrganisationTypeValidator implements BusinessRuleValidator {
    private static final CIString OTHER = ciString("OTHER");

    private final Maintainers maintainers;

    @Autowired
    public OrganisationTypeValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ORGANISATION);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.isOverride()) {
            return;
        }

        final CIString orgType = update.getUpdatedObject().getValueForAttribute(AttributeType.ORG_TYPE);

        if (!OTHER.equals(orgType)) {
            final Set<CIString> mntBys = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
            final boolean hasOnlyPowerMaintainer = Sets.intersection(maintainers.getPowerMaintainers(), mntBys).containsAll(mntBys);

            if (!hasOnlyPowerMaintainer || !updateContext.getSubject(update).hasPrincipal(Principal.POWER_MAINTAINER)) {
                updateContext.addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
            }
        }
    }
}
