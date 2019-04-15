package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MustKeepAbuseMailboxIfReferencedValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.DELETE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROLE);

    private static final Set<ObjectType> REFERENCED_OBJECT_TYPES = ImmutableSet.of(
        ObjectType.ORGANISATION, ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM
    );

    private final RpslObjectUpdateDao updateObjectDao;
    private final RpslObjectDao objectDao;

    @Autowired
    public MustKeepAbuseMailboxIfReferencedValidator(final RpslObjectUpdateDao updateObjectDao, final RpslObjectDao objectDao) {
        this.updateObjectDao = updateObjectDao;
        this.objectDao = objectDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<CIString> originalAbuseMailbox = update.getReferenceObject().getValuesForAttribute(AttributeType.ABUSE_MAILBOX);
        final Set<CIString> updatedAbuseMailbox = update.getUpdatedObject().getValuesForAttribute(AttributeType.ABUSE_MAILBOX);

        if (!updatedAbuseMailbox.isEmpty() || originalAbuseMailbox.isEmpty()) {
            return;
        }

        for (final RpslObjectInfo referenceInfo : updateObjectDao.getReferences(update.getUpdatedObject())) {
            if (REFERENCED_OBJECT_TYPES.contains(referenceInfo.getObjectType())) {
                final Set<CIString> abuseCAttributes = objectDao.getById(referenceInfo.getObjectId()).getValuesForAttribute(AttributeType.ABUSE_C);
                if (!abuseCAttributes.isEmpty() && abuseCAttributes.contains(update.getUpdatedObject().getValueForAttribute(AttributeType.NIC_HDL))) {
                    updateContext.addMessage(update, UpdateMessages.abuseMailboxReferenced(update.getUpdatedObject().getValueForAttribute(AttributeType.ROLE), referenceInfo.getObjectType()));
                    break;
                }
            }
        }
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
