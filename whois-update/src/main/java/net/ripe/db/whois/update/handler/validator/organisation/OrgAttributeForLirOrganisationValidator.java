package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class OrgAttributeForLirOrganisationValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final CIString LIR = ciString("LIR");

    private final Maintainers maintainers;

    @Autowired
    public OrgAttributeForLirOrganisationValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString orgType = update.getReferenceObject().getValueOrNullForAttribute(AttributeType.ORG_TYPE);

        if(!LIR.equals(orgType) || updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        if(orgChanged(update.getReferenceObject(), update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG));
        }
    }

    private boolean orgChanged(final RpslObject originalObject, final RpslObject updatedObject) {
        final CIString originalOrg = originalObject.getValueOrNullForAttribute(AttributeType.ORG);
        final CIString updatedOrg = updatedObject.getValueOrNullForAttribute(AttributeType.ORG);

        return !Objects.equals(originalOrg, updatedOrg);
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
