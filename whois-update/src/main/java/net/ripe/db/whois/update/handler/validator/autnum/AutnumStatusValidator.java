package net.ripe.db.whois.update.handler.validator.autnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class AutnumStatusValidator implements BusinessRuleValidator {

    private static final List<Action> ACTIONS = Collections.unmodifiableList(Lists.newArrayList(Action.CREATE, Action.MODIFY));
    private static final List<ObjectType> TYPES = Collections.unmodifiableList(Lists.newArrayList(ObjectType.AUT_NUM));

    private final Maintainers maintainers;

    @Autowired
    public AutnumStatusValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public List<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public void validate(PreparedUpdate update, UpdateContext updateContext) {

        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        switch (update.getAction()) {
            case CREATE:
                validateCreate(update, updateContext);
                return;
            case MODIFY:
                validateModify(update, updateContext);
                return;
            default:
                return;
        }
    }

    private void validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<CIString> mntBy = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
        final boolean mntByRs = !Sets.intersection(maintainers.getRsMaintainers(), mntBy).isEmpty();

        if (update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {

            final AutnumStatus status = AutnumStatus.valueOf(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());

            if (mntByRs) {
                if (status != AutnumStatus.ASSIGNED) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeAssigned(status));
                }
            } else {
                if (status != AutnumStatus.OTHER) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeOther(status));
                    return;
                }
            }

        } else {

            if (!mntByRs) {
                // TODO: generated OTHER if not specified?
                updateContext.addMessage(update, ValidationMessages.missingMandatoryAttribute(AttributeType.STATUS));
                return;
            } else {
                // TODO: rs maintainer, generate ASSIGNED status
            }

        }

    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {

        final Set<CIString> mntBy = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
        final boolean mntByRs = !Sets.intersection(maintainers.getRsMaintainers(), mntBy).isEmpty();

        if (update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {
            final AutnumStatus status = AutnumStatus.valueOf(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());

            if (update.getReferenceObject().containsAttribute(AttributeType.STATUS)) {
                final AutnumStatus previousStatus = AutnumStatus.valueOf(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());

                if (!mntByRs) {
                    if ((previousStatus == AutnumStatus.LEGACY) && (status == AutnumStatus.ASSIGNED)) {
                        updateContext.addMessage(update, UpdateMessages.statusCanOnlyBeChangedByRsMaintainer());
                        return;
                    }
                }
            } else {
                if (!mntByRs) {
                    updateContext.addMessage(update, UpdateMessages.statusCanOnlyBeAddedByRsMaintainer(status));
                    return;
                }
            }

            if (!mntByRs) {
                if (status != AutnumStatus.OTHER) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeOther(status));
                    return;
                }
            } else {
                if (status != AutnumStatus.ASSIGNED) {
                    updateContext.addMessage(update, UpdateMessages.invalidStatusMustBeAssigned(status));
                    return;
                }
            }

        } else {

            if (update.getReferenceObject().containsAttribute(AttributeType.STATUS)) {
                if (mntByRs) {
                    // TODO: generate ASSIGNED status
                } else {
                    final AutnumStatus previousStatus = AutnumStatus.valueOf(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS).toUpperCase());
                    updateContext.addMessage(update, UpdateMessages.statusCannotBeRemoved(previousStatus));
                    return;
                }
            }
        }
    }

}
