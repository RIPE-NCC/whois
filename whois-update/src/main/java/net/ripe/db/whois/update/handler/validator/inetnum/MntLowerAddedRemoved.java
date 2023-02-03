package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;


@Component
public class MntLowerAddedRemoved implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private static final List<InetnumStatus> VALIDATED_INETNUM_STATUSES = ImmutableList.of(InetnumStatus.ASSIGNED_ANYCAST);
    private static final List<Inet6numStatus> VALIDATED_INET6NUM_STATUSES = ImmutableList.of(Inet6numStatus.ASSIGNED_PI, Inet6numStatus.ASSIGNED_ANYCAST);

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        if (ObjectType.INETNUM.equals(update.getType()) && !VALIDATED_INETNUM_STATUSES.contains(getStatus(update))) {
            return Collections.emptyList();
        }

        if (ObjectType.INET6NUM.equals(update.getType()) && !VALIDATED_INET6NUM_STATUSES.contains(getStatus(update))) {
            return Collections.emptyList();
        }

        final Set<CIString> differences = update.getDifferences(AttributeType.MNT_LOWER);
        if (!differences.isEmpty()) {
            return Arrays.asList(new CustomValidationMessage(UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER)));
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
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
