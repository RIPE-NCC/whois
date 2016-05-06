package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class OrganisationTypeValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final CIString OTHER = ciString("OTHER");
    private static final CIString LIR = ciString("LIR");

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final RpslAttribute attribute = update.getUpdatedObject().findAttribute(AttributeType.ORG_TYPE);
        final CIString updatedOrgType = attribute.getCleanValue();

        if(update.getAction() == Action.CREATE) {
            if (!OTHER.equals(updatedOrgType) && !subject.hasPrincipal(Principal.POWER_MAINTAINER)) {
                updateContext.addMessage(update, attribute, UpdateMessages.invalidMaintainerForOrganisationType(updatedOrgType));
            }

        }
        else if(update.getAction() == Action.MODIFY) {
            final CIString originalOrgType = update.getReferenceObject().getValueForAttribute(AttributeType.ORG_TYPE);

            if (!OTHER.equals(updatedOrgType) && !LIR.equals(originalOrgType) && orgTypeHasChanged(update, updatedOrgType) && !subject.hasPrincipal(Principal.POWER_MAINTAINER)) {
                updateContext.addMessage(update, attribute, UpdateMessages.invalidMaintainerForOrganisationType(updatedOrgType));
            }

            if (LIR.equals(originalOrgType) && orgTypeHasChanged(update, updatedOrgType) && !subject.hasPrincipal(Principal.POWER_MAINTAINER)) {
                updateContext.addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_TYPE));
            }
        }



    }

    private boolean orgTypeHasChanged(final PreparedUpdate update, final CIString orgTypeUpdatedObject) {
        return !update.getReferenceObject().getValueForAttribute(AttributeType.ORG_TYPE).equals(orgTypeUpdatedObject);
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
