package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class CreateRipeNccMaintainerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);

    private final Set<CIString> ripeMaintainers;

    @Autowired
    public CreateRipeNccMaintainerValidator(final Maintainers maintainers) {
        ripeMaintainers = Sets.newHashSet(maintainers.getEnduserMaintainers());
        ripeMaintainers.addAll(maintainers.getLegacyMaintainers());
        ripeMaintainers.addAll(maintainers.getAllocMaintainers());
        ripeMaintainers.addAll(maintainers.getEnumMaintainers());
        ripeMaintainers.addAll(maintainers.getDbmMaintainers());
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        if (ripeMaintainers.contains(updatedObject.getKey())) {
            return Arrays.asList(UpdateMessages.creatingRipeMaintainerForbidden());
        }

        return Collections.emptyList();
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
