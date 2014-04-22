package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.Lists;
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
        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final RpslAttribute attribute = update.getUpdatedObject().findAttribute(AttributeType.ORG_TYPE);
        final CIString orgType = attribute.getCleanValue();

        if (!OTHER.equals(orgType) && orgTypeHasChanged(update, orgType) && !subject.hasPrincipal(Principal.POWER_MAINTAINER)) {
            updateContext.addMessage(update, attribute, UpdateMessages.invalidMaintainerForOrganisationType(orgType));
        }
    }

    private boolean orgTypeHasChanged(final PreparedUpdate update, final CIString orgTypeUpdatedObject) {
        if (update.getAction() == Action.CREATE) {
            return true;
        }

        return !update.getReferenceObject().getValueForAttribute(AttributeType.ORG_TYPE).equals(orgTypeUpdatedObject);
    }
}
