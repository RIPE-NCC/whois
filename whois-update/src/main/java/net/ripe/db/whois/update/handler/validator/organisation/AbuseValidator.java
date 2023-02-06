package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Component
public class AbuseValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ORGANISATION, INETNUM, INET6NUM, AUT_NUM);

    private final RpslObjectDao objectDao;
    private final RpslObjectUpdateDao updateDao;
    private Maintainers maintainers;

    @Autowired
    public AbuseValidator(final RpslObjectDao objectDao, final RpslObjectUpdateDao updateDao, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
        this.updateDao = updateDao;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return Collections.emptyList();
        }

        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();

        validateAbuseC(updatedObject, customValidationMessages);
        validateAbuseCRemoved(updatedObject, update, customValidationMessages);

        return customValidationMessages;
    }

    private void validateAbuseC(final RpslObject updatedObject, final List<CustomValidationMessage> customValidationMessages) {
        final CIString abuseC = updatedObject.getValueOrNullForAttribute(AttributeType.ABUSE_C);
        if (abuseC == null) {
            return;
        }

        try {
            final RpslObject abuseCRole = objectDao.getByKey(ObjectType.ROLE, abuseC);
            if (!abuseCRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.abuseMailboxRequired(abuseC, updatedObject.getType())));
            }
        } catch (EmptyResultDataAccessException e) {
            try {
                objectDao.getByKey(ObjectType.PERSON, abuseC);
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.abuseCPersonReference(), null));
            } catch (EmptyResultDataAccessException e1) {
                // ignore, invalid reference type is checked elsewhere
                LOGGER.debug("{}: {}", e1.getClass().getName(), e1.getMessage());
            }
        }
    }

    private void validateAbuseCRemoved(final RpslObject updatedObject, final PreparedUpdate update, final List<CustomValidationMessage> customValidationMessages) {
        if (updatedObject.getType() == ORGANISATION &&
            isAbuseCRemoved(updatedObject, update) &&
            (isLir(update.getReferenceObject()) ||
                    isOrgReferencedByRsMaintainedResources(updatedObject))) {
            customValidationMessages.add(new CustomValidationMessage(UpdateMessages.abuseContactNotRemovable(), null));
        }
    }

    private boolean isAbuseCRemoved(final RpslObject updatedObject, final PreparedUpdate update) {
        return ((update.getAction() == Action.MODIFY) &&
                (!updatedObject.containsAttribute(AttributeType.ABUSE_C)) &&
                (update.getReferenceObject() != null) &&
                (update.getReferenceObject().containsAttribute(AttributeType.ABUSE_C)));
    }

    private boolean isOrgReferencedByRsMaintainedResources(final RpslObject updatedObject) {
        return updateDao.getReferences(updatedObject)
            .stream()
            .filter(Objects::nonNull)
            .filter(rpslObjectInfo -> ObjectType.RESOURCE_TYPES.contains(rpslObjectInfo.getObjectType()))
            .anyMatch(rpslObjectInfo -> {
                final RpslObject referencingObject = objectDao.getById(rpslObjectInfo.getObjectId());
                return maintainers.isRsMaintainer(referencingObject.getValuesForAttribute(AttributeType.MNT_BY));
            });
    }

    private boolean isLir(final RpslObject organisation) {
        return OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
    }
}
