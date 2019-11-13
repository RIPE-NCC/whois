package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class CountryRipeMaintainedValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE, MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ORGANISATION);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return;
        }

        final RpslObject originalObject = update.getReferenceObject();

        final RpslObject updatedObject = update.getUpdatedObject();

        if (hasCountryChanged(originalObject, updatedObject, update.getAction())) {
            updateContext.addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(COUNTRY));
        }
    }

    private boolean hasCountryChanged(final RpslObject originalObject, final RpslObject updatedObject, final Action action) {
        return (action == CREATE && updatedObject.containsAttribute(COUNTRY)) || // create
                (originalObject.containsAttribute(COUNTRY) && updatedObject.containsAttribute(COUNTRY) &&
                 !originalObject.getValuesForAttribute(COUNTRY).equals(updatedObject.getValuesForAttribute(COUNTRY))) || // modify country
                (originalObject.containsAttribute(COUNTRY) && !updatedObject.containsAttribute(COUNTRY)) || // remove country
                (!originalObject.containsAttribute(COUNTRY) && updatedObject.containsAttribute(COUNTRY)); // add country
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
