package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.attrs.InetnumStatus;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;


@Component
public class MntLowerAddedRemoved implements BusinessRuleValidator {
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

        if (ObjectType.INETNUM.equals(update.getType()) && !InetnumStatus.ASSIGNED_ANYCAST.equals(getStatus(update))) {
            return;
        }

        if (ObjectType.INET6NUM.equals(update.getType()) && !getStatus(update).requiresRsMaintainer()) {
            return;
        }

        final Set<CIString> differences = update.getDifferences(AttributeType.MNT_LOWER);
        if (!differences.isEmpty() && !updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
            updateContext.addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
        }
    }
}
