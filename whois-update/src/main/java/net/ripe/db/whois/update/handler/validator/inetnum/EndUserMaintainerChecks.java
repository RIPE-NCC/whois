package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.Maintainers;
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

@Component
public class EndUserMaintainerChecks implements BusinessRuleValidator {
    private final Maintainers maintainers;

    @Autowired
    public EndUserMaintainerChecks(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.isOverride()) {
            return;
        }

        if (updateContext.getSubject(update).hasPrincipal(Principal.ENDUSER_MAINTAINER)) {
            final boolean hasEnduserMaintainers = !Sets.intersection(
                    maintainers.getEnduserMaintainers(),
                    update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY)).isEmpty();

            if (!hasEnduserMaintainers) {
                updateContext.addMessage(update, UpdateMessages.adminMaintainerRemoved());
            }
        }
    }
}
