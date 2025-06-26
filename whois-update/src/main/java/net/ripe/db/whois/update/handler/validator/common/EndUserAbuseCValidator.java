package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;

@Component
public class EndUserAbuseCValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);

    private static final CIString ASSIGNED_PI = ciString("ASSIGNED PI");
    private static final CIString LEGACY = ciString("LEGACY");

    private final RpslObjectDao rpslObjectDao;
    private final Maintainers maintainers;

    @Autowired
    public EndUserAbuseCValidator(final RpslObjectDao rpslObjectDao,
                                  final Maintainers maintainers) {
        this.rpslObjectDao = rpslObjectDao;
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString org = update.getUpdatedObject().getValueOrNullForAttribute(AttributeType.ORG);
        if (org == null) {
            return Collections.emptyList();
        }

        final RpslObject organisation = rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, org);
        if (organisation == null) {
            return Collections.emptyList();
        }

        if (OrgType.OTHER != OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE))) {
            return Collections.emptyList();
        }

        if (!shouldCheckBasedOnStatus(update.getSubmittedObject())) {
            return Collections.emptyList();
        }

        if (organisation.getValueOrNullForAttribute(AttributeType.ABUSE_C) == null) {
            return List.of(UpdateMessages.noAbuseContact(org));
        } else {
            final RpslObject abuseContact = rpslObjectDao.getByKeyOrNull(ROLE, organisation.getValueForAttribute(AttributeType.ABUSE_C));

            if (abuseContact == null) {
                return List.of(UpdateMessages.abuseCPersonReference());
            } else {
                if (!abuseContact.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                    return List.of(UpdateMessages.abuseMailboxRequired(abuseContact.getKey(), update.getUpdatedObject().getType()));
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean shouldCheckBasedOnStatus(final RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(STATUS)) {
            return false;
        }

        if (rpslObject.getType() == AUT_NUM) {
            return true;
        }

        if (ASSIGNED_PI.equals(rpslObject.getValueForAttribute(STATUS))) {
            return true;
        }

        return isLegacyUnderContract(rpslObject);
    }

    private boolean isLegacyUnderContract(final RpslObject rpslObject) {
        if (!LEGACY.equals(rpslObject.getValueForAttribute(STATUS))) {
            return false;
        }

        return maintainers.isRsMaintainer(rpslObject.getValuesForAttribute(AttributeType.MNT_BY));
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
