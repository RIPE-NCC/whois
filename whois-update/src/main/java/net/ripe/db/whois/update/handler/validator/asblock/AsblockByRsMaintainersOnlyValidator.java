package net.ripe.db.whois.update.handler.validator.asblock;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AsblockByRsMaintainersOnlyValidator implements BusinessRuleValidator {

    @Override
    public List<Action> getActions() {
        return ImmutableList.of(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return ImmutableList.of(ObjectType.AS_BLOCK);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final boolean authenticatedByOverride = updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);
        final boolean authenticatedByDbmMaintainer = updateContext.getSubject(update).hasPrincipal(Principal.DBM_MAINTAINER);
        if (!(authenticatedByOverride || authenticatedByDbmMaintainer)) {
            updateContext.addMessage(update, UpdateMessages.asblockIsMaintainedByRipe());
        }
    }
}
