package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
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

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

@Component
public class FirstDescriptionChanged implements BusinessRuleValidator {
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

        final RpslObject originalObject = update.getReferenceObject();
        final CIString originalFirstDescription = originalObject.findAttributes(AttributeType.DESCR).get(0).getCleanValue();
        final CIString updatedFirstDescription = update.getUpdatedObject().findAttributes(AttributeType.DESCR).get(0).getCleanValue();

        final boolean statusRequiresEndMntnerAuth = getStatus(update).requiresRsMaintainer();
        final boolean hasEndMntnerAuth = updateContext.getSubject(update).hasPrincipal(Principal.ENDUSER_MAINTAINER);
        if (statusRequiresEndMntnerAuth && !hasEndMntnerAuth && !originalFirstDescription.equals(updatedFirstDescription)) {
            updateContext.addMessage(update, UpdateMessages.authorisationRequiredForFirstAttrChange(AttributeType.DESCR));
        }
    }
}
