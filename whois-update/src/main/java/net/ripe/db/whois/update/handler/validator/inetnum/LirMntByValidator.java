package net.ripe.db.whois.update.handler.validator.inetnum;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
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
public class LirMntByValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final Maintainers maintainers;

    @Autowired
    public LirMntByValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        final RpslObject originalObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        final boolean rsMaintained = maintainers.isRsMaintainer(originalObject.getValuesForAttribute(AttributeType.MNT_BY));

        if (mntByChanged(originalObject, updatedObject) && rsMaintained && isAllocation(originalObject)) {
            if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.RS_MAINTAINER)) {
                return;
            } else {
                updateContext.addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
            }
        }
    }

    private boolean mntByChanged(final RpslObject originalObject, final RpslObject updatedObject) {
        return !originalObject.getValuesForAttribute(AttributeType.MNT_BY)
                .equals(updatedObject.getValuesForAttribute(AttributeType.MNT_BY));
    }

    private boolean isAllocation(final RpslObject originalObject) {
        InetStatus status;

        if (originalObject.getType() == ObjectType.INETNUM) {
            status = InetnumStatus.getStatusFor(originalObject.getValueForAttribute(AttributeType.STATUS));
        } else {
            status = Inet6numStatus.getStatusFor(originalObject.getValueForAttribute(AttributeType.STATUS));
        }

        // TODO - To make it consistent, we can check for RIPE-NCC-HM-MNT
        return InetnumStatus.ALLOCATED_PA.equals(status) || InetnumStatus.ALLOCATED_PI.equals(status) ||
                InetnumStatus.ALLOCATED_UNSPECIFIED.equals(status) || Inet6numStatus.ALLOCATED_BY_RIR.equals(status);
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
