package net.ripe.db.whois.update.handler.validator.autnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.AutnumStatus;
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
public class AutnumStatusValidator implements BusinessRuleValidator {

    private final Maintainers maintainers;

    @Autowired
    public AutnumStatusValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.AUT_NUM);
    }

    @Override
    public void validate(PreparedUpdate update, UpdateContext updateContext) {

        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        switch (update.getAction()) {
            case CREATE:
                validateCreate(update, updateContext);
                break;
            case MODIFY:
                validateModify(update, updateContext);
                break;
        }
    }

    private void validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (!update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {
            updateContext.addMessage(update, ValidationMessages.missingMandatoryAttribute(AttributeType.STATUS));
            return;
        }

        final AutnumStatus status = AutnumStatus.valueOf(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());
        final boolean mntByRs = isMaintainedByRsMaintainer(update.getUpdatedObject());

        if (mntByRs) {
            if (status != AutnumStatus.ASSIGNED) {
                updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeAssigned(status));
            }
        } else {
            if (status != AutnumStatus.OTHER) {
                updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeOther());
                return;
            }
        }
    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {
            final AutnumStatus status = AutnumStatus.valueOf(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());
            final boolean mntByRs = isMaintainedByRsMaintainer(update.getReferenceObject());

            if (update.getReferenceObject().containsAttribute(AttributeType.STATUS)) {
                final AutnumStatus previousStatus = AutnumStatus.valueOf(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());

                if ((previousStatus == AutnumStatus.LEGACY) && (status == AutnumStatus.ASSIGNED)) {
                    updateContext.addMessage(update, UpdateMessages.statusCanOnlyBeChangedByOverride(AutnumStatus.LEGACY, AutnumStatus.ASSIGNED));
                    return;
                }
            } else {
                if (mntByRs && !isAuthorisedByRsMaintainer(update, updateContext)) {
                    updateContext.addMessage(update, UpdateMessages.statusCanOnlyBeAddedByRsMaintainer(status));
                    return;
                }
            }

            if (mntByRs) {
                if (status != AutnumStatus.ASSIGNED) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeAssigned(status));
                    return;
                }
            } else {
                if (status == AutnumStatus.ASSIGNED) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusCannotBeAssigned());
                    return;
                }
            }

        } else {
            if (update.getReferenceObject().containsAttribute(AttributeType.STATUS)) {
                final AutnumStatus previousStatus = AutnumStatus.valueOf(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());
                updateContext.addMessage(update, UpdateMessages.statusCannotBeRemoved(previousStatus));
                return;
            }
        }
    }

    private boolean isMaintainedByRsMaintainer(final RpslObject object) {
        final Set<CIString> mntBy = object.getValuesForAttribute(AttributeType.MNT_BY);
        return !Sets.intersection(maintainers.getRsMaintainers(), mntBy).isEmpty();
    }

    private boolean isAuthorisedByRsMaintainer(final PreparedUpdate update, final UpdateContext updateContext) {
        return updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);
    }
}
