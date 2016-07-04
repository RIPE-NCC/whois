package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
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

import java.util.Set;
import java.util.stream.Collectors;

@Component
// Validates that their is at most 1 single user mntner for an LIR
public class LirMntByAttributeCountValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final CIString LIR = CIString.ciString("LIR");

    private final Maintainers maintainers;

    @Autowired
    public LirMntByAttributeCountValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject originalObject = update.getReferenceObject();
        if (!LIR.equals(originalObject.getValueForAttribute(AttributeType.ORG_TYPE))) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        Set<CIString> userMntner = filterUserMntner(updatedObject);
        if (1 < userMntner.size()) {
            updateContext.addMessage(update, UpdateMessages.multipleUserMntBy(userMntner));
        }
    }

    private Set<CIString> filterUserMntner(RpslObject rpslObject) {
        return rpslObject.getValuesForAttribute(AttributeType.MNT_BY)
                .stream()
                .filter(mntby -> !maintainers.isRsMaintainer(mntby))
                .collect(Collectors.toSet());
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
