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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
// Validates that RIPE NCC maintained attributes are not changed for an LIR
// Possible ways to change it are by override or power mntner.
public class LirRipeMaintainedAttributesValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LirRipeMaintainedAttributesValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final List<AttributeType> RIPE_NCC_MANAGED_ATTRIBUTES = ImmutableList.of(
            AttributeType.MNT_BY,
            AttributeType.ORG,
            AttributeType.COUNTRY,
            AttributeType.ORG_TYPE);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        LOGGER.info("Performing validation for managed attribute, testing country modification");
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject originalObject = update.getReferenceObject();
        if (!isLir(originalObject)) {
            return Collections.emptyList();
        }

        LOGGER.info("It is an LIR so will perfom validation");


        List<Message> messages = Lists.newArrayList();
        final RpslObject updatedObject = update.getUpdatedObject();
        RIPE_NCC_MANAGED_ATTRIBUTES.forEach(attributeType -> {

            LOGGER.info("checking for attribute {} ", attributeType );

            if (haveAttributesChanged(originalObject, updatedObject, attributeType)) {
                LOGGER.info("attribute {} has changed so adding exception ", attributeType );

                messages.add(UpdateMessages.canOnlyBeChangedByRipeNCC(attributeType));
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
        LOGGER.info("attribute check for case insensitive {}- {}", originalObject.getValuesForAttribute(attributeType), updatedObject.getValuesForAttribute(attributeType) );

        return !originalObject.getValuesForAttribute(attributeType)
                    .equals(updatedObject.getValuesForAttribute(attributeType));
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
