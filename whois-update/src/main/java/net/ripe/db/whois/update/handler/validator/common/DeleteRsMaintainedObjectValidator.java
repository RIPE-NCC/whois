package net.ripe.db.whois.update.handler.validator.common;

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
public class DeleteRsMaintainedObjectValidator implements BusinessRuleValidator {
    private final Maintainers maintainers;

    @Autowired
    public DeleteRsMaintainedObjectValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.isOverride()) {
            return;
        }

        if (updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
            return;
        }

        final Set<CIString> mntBys = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
        if (!Sets.intersection(maintainers.getRsMaintainers(), mntBys).isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.authorisationRequiredForDeleteRsMaintainedObject());
        }
    }
}
