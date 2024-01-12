package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LirUserMaintainedAttributesValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final List<AttributeType> USER_MANAGED_IN_PORTAL_ATTRIBUTES = ImmutableList.of(
            AttributeType.ADDRESS,
            AttributeType.PHONE,
            AttributeType.FAX_NO,
            AttributeType.E_MAIL,
            AttributeType.ORG_NAME);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject originalObject = update.getReferenceObject();
        if (! isLir(originalObject)) {
            return Collections.emptyList();
        }

        List<Message> messages = Lists.newArrayList();
        final RpslObject updatedObject = update.getUpdatedObject();
        USER_MANAGED_IN_PORTAL_ATTRIBUTES.forEach(attributeType -> {
            if (haveAttributesChanged(originalObject, updatedObject, attributeType)) {
                messages.add(UpdateMessages.canOnlyBeChangedinLirPortal(attributeType));
            }
        });

        return messages;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

    private boolean isLir(final RpslObject organisation) {
        return OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
    }

    private boolean haveAttributesChanged(final RpslObject originalObject, final RpslObject updatedObject, final AttributeType attributeType) {
        if (AttributeType.ORG_NAME == attributeType) {
            return haveAttributesChanged(originalObject, updatedObject, attributeType, true);
        }

        return haveAttributesChanged(originalObject, updatedObject, attributeType, false);
    }

    private boolean haveAttributesChanged(final RpslObject originalObject, final RpslObject updatedObject, final AttributeType attributeType, final boolean caseSensitive) {
        if (caseSensitive) {
            return !mapToStrings(originalObject.getValuesForAttribute(attributeType))
                        .equals(mapToStrings(updatedObject.getValuesForAttribute(attributeType)));
        }

        return !originalObject.getValuesForAttribute(attributeType)
                    .equals(updatedObject.getValuesForAttribute(attributeType));
    }

    final Set<String> mapToStrings(final Set<CIString> values) {
        return values.stream().map(ciString -> ciString.toString()).collect(Collectors.toSet());
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
