package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
// Validates that their is at most 1 single user mntner for an LIR
public class LirMntByAttributeCountValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private final Maintainers maintainers;

    @Autowired
    public LirMntByAttributeCountValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject originalObject = update.getReferenceObject();
        if (!isLir(originalObject)) {
            return Collections.emptyList();
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        final Collection<CIString> userMntner = filterUserMntner(updatedObject);
        if (userMntner.size() > 1) {
            return Arrays.asList(UpdateMessages.multipleUserMntBy(userMntner));
        }

        return Collections.emptyList();
    }

    private Collection<CIString> filterUserMntner(final RpslObject rpslObject) {
        return rpslObject.getValuesForAttribute(AttributeType.MNT_BY)
                .stream()
                .filter(mntby -> !maintainers.isRsMaintainer(mntby))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private boolean isLir(final RpslObject organisation) {
        return OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
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
