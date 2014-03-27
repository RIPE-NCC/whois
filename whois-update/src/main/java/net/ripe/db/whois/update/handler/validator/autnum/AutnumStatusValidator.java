package net.ripe.db.whois.update.handler.validator.autnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
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
        return Lists.newArrayList(Action.MODIFY);
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
                return;
            case MODIFY:
                validateModify(update, updateContext);
                return;
            default:
                return;
        }
    }

    private void validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {

    }

    private void validateModify(PreparedUpdate update, UpdateContext updateContext) {

        if (update.getReferenceObject().containsAttribute(AttributeType.STATUS) &&
                !update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {
            updateContext.addMessage(update, UpdateMessages.statusCannotBeRemoved(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS)));
            return;
        }

        if (!update.getReferenceObject().containsAttribute(AttributeType.STATUS) &&
                update.getUpdatedObject().containsAttribute(AttributeType.STATUS)) {
            final Set<CIString> mntBy = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
            if (Sets.intersection(maintainers.getRsMaintainers(), mntBy).isEmpty()) {
                updateContext.addMessage(update, UpdateMessages.statusCannotBeAdded(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS)));
                return;
            }
        }
    }
}
