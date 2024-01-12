package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);

    private final Maintainers maintainers;

    @Autowired
    public OrgAttributeNotChangedValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {

        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }
        final RpslObject originalObject = update.getReferenceObject();
        final CIString originalOrg = originalObject.getValueOrNullForAttribute(AttributeType.ORG);
        final CIString updatedOrg = update.getUpdatedObject().getValueOrNullForAttribute(AttributeType.ORG);

        if (Objects.equals(originalOrg, updatedOrg)) {
            return Collections.emptyList();
        }

        boolean rsMaintained = maintainers.isRsMaintainer(originalObject.getValuesForAttribute(AttributeType.MNT_BY));

        final List<Message> customValidationMessages = Lists.newArrayList();
        if (rsMaintained) {
            final List<RpslAttribute> org = update.getUpdatedObject().findAttributes(AttributeType.ORG);
            if (org.isEmpty()) {
                customValidationMessages.add(UpdateMessages.cantRemoveOrgAttribute());
            } else {
                customValidationMessages.add(UpdateMessages.cantChangeOrgAttribute(org.get(0)));
            }
        }

        return customValidationMessages;
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
