package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnumDomainAuthorisationValidator implements BusinessRuleValidator {
    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.DOMAIN);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.isOverride()) {
            return;
        }

        final RpslObject rpslObject = update.getUpdatedObject();
        final CIString domainString = rpslObject.getKey();
        final Domain domain = Domain.parse(domainString);
        if (domain.getType() == Domain.Type.E164) {
            if (!updateContext.getSubject(update).hasPrincipal(Principal.ENUM_MAINTAINER)) {
                updateContext.addMessage(update, UpdateMessages.authorisationRequiredForEnumDomain());
            }
        }
    }
}
