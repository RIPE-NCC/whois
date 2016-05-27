package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EndUserMaintainerChecks implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final Maintainers maintainers;

    @Autowired
    public EndUserMaintainerChecks(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        if (subject.hasPrincipal(Principal.ENDUSER_MAINTAINER)) {
            final boolean hasEnduserMaintainers = !Sets.intersection(
                    maintainers.getEnduserMaintainers(),
                    update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY)).isEmpty();

            if (!hasEnduserMaintainers) {
                updateContext.addMessage(update, UpdateMessages.adminMaintainerRemoved());
            }
        }
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
