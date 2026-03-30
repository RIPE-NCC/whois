package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
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
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

/**
 * Add an ERROR message if an "mnt-lower:" attribute is added to inet(6)num assignments.
 * No error if the "mnt-lower:" already exists on the inet(6)num (but was not added by the update). This needs to be cleaned up.
 */
@Component
public class MntLowerAddedValidator implements BusinessRuleValidator {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);
    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final List<InetnumStatus> INETNUM_MNT_LOWER_NOT_ALLOWED = ImmutableList.of(InetnumStatus.ASSIGNED_PA, InetnumStatus.ASSIGNED_PI, InetnumStatus.ASSIGNED_ANYCAST);
    private static final List<Inet6numStatus> INET6NUM_MNT_LOWER_NOT_ALLOWED = ImmutableList.of(Inet6numStatus.ASSIGNED_PI, Inet6numStatus.ASSIGNED, Inet6numStatus.ASSIGNED_ANYCAST);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        if (mntLowerNotAllowed(update) && mntLowerAdded(update)) {
            return addErrorMessage(update);
        }

        return Collections.emptyList();
    }

    private boolean mntLowerAdded(final PreparedUpdate update) {
        if (update.hasOriginalObject()) {
            return !update.getNewValues(AttributeType.MNT_LOWER).isEmpty();
        } else {
            return !update.getDifferences(AttributeType.MNT_LOWER).isEmpty();
        }
    }

    private List<Message> addErrorMessage(final PreparedUpdate update) {
        return List.of(
            UpdateMessages.attributeNotAllowedWithStatus(
                AttributeType.MNT_LOWER,
                update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS)));
    }

    protected boolean mntLowerNotAllowed(final PreparedUpdate update){
        return INETNUM_MNT_LOWER_NOT_ALLOWED.contains(getStatus(update)) ||
                INET6NUM_MNT_LOWER_NOT_ALLOWED.contains(getStatus(update));
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

}
