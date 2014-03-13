package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class OrganisationTypeValidator implements BusinessRuleValidator {
    private static final CIString OTHER = ciString("OTHER");

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
        final boolean authPowerMntner = updateContext.getSubject(update).hasPrincipal(Principal.POWER_MAINTAINER);


        if (!OTHER.equals(orgType)) {
            if (orgTypeHasChanged(update, orgType) && !authPowerMntner) {
                updateContext.addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
            }
        }
    }

    private boolean orgTypeHasChanged(final PreparedUpdate update, final CIString orgTypeUpdatedObject) {
        if (update.getAction() == Action.CREATE) {
            return true;
        }

        return !update.getReferenceObject().getValueForAttribute(AttributeType.ORG_TYPE).equals(orgTypeUpdatedObject);
    }
}
