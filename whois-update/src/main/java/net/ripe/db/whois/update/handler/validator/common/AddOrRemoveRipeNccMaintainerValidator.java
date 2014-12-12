package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
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

@Component
public class AddOrRemoveRipeNccMaintainerValidator implements BusinessRuleValidator {
    private final Maintainers maintainers;

    @Autowired
    public AddOrRemoveRipeNccMaintainerValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        validateForSpecialMaintainer(Principal.RS_MAINTAINER, maintainers.getRsMaintainers(), update, updateContext);
        validateForSpecialMaintainer(Principal.DBM_MAINTAINER, maintainers.getDbmMaintainers(), update, updateContext);
    }

    private void validateForSpecialMaintainer(final Principal principal, final Set<CIString> specialMaintainers, final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(principal)) {
            return;
        }

        final Set<CIString> differentMaintainers = Sets.newLinkedHashSet();
        differentMaintainers.addAll(update.getDifferences(AttributeType.MNT_BY));
        differentMaintainers.addAll(update.getDifferences(AttributeType.MNT_DOMAINS));
        differentMaintainers.addAll(update.getDifferences(AttributeType.MNT_LOWER));

        for (final CIString mntRouteString : update.getDifferences(AttributeType.MNT_ROUTES)) {
            differentMaintainers.add(MntRoutes.parse(mntRouteString).getMaintainer());
        }

        if (!Sets.intersection(differentMaintainers, specialMaintainers).isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
        }
    }
}
