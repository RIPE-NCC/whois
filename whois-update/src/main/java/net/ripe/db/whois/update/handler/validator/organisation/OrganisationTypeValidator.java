package net.ripe.db.whois.update.handler.validator.organisation;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

@Component
public class OrganisationTypeValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final RpslAttribute orgTypeAttribute = update.getUpdatedObject().findAttribute(AttributeType.ORG_TYPE);
        final CIString orgType = orgTypeAttribute.getCleanValue();

        if ((OrgType.OTHER != OrgType.getFor(orgType)) &&
                orgTypeHasChanged(update) &&
                !subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            updateContext.addMessage(update, orgTypeAttribute, UpdateMessages.invalidMaintainerForOrganisationType(orgType));
        }
    }

    private boolean orgTypeHasChanged(final PreparedUpdate update) {
        if (update.getAction() == Action.CREATE) {
            return true;
        }

        return !update.getReferenceObject().getValueForAttribute(AttributeType.ORG_TYPE)
            .equals(update.getUpdatedObject().getValueForAttribute(AttributeType.ORG_TYPE));
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
