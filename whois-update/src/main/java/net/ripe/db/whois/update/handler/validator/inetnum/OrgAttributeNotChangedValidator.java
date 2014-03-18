package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class OrgAttributeNotChangedValidator implements BusinessRuleValidator {
    private final Maintainers maintainers;

    @Autowired
    public OrgAttributeNotChangedValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Collections.singletonList(Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject originalObject = update.getReferenceObject();
        final CIString originalOrg = originalObject.getValueOrNullForAttribute(AttributeType.ORG);
        final CIString updatedOrg = update.getUpdatedObject().getValueOrNullForAttribute(AttributeType.ORG);

        if (Objects.equals(originalOrg, updatedOrg)) {
            return;
        }

        boolean rsMaintained = !Sets.intersection(this.maintainers.getRsMaintainers(), originalObject.getValuesForAttribute(AttributeType.MNT_BY)).isEmpty();

        final Subject subject = updateContext.getSubject(update);
        if (rsMaintained && !(subject.hasPrincipal(Principal.RS_MAINTAINER) || subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER))) {
            updateContext.addMessage(update, UpdateMessages.cantChangeOrgAttribute());
        }
    }
}
