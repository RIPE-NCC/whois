package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
public class MustKeepAbuseMailboxIfReferencedValidator implements BusinessRuleValidator {

    private final RpslObjectUpdateDao updateObjectDao;
    private final RpslObjectDao objectDao;

    @Autowired
    public MustKeepAbuseMailboxIfReferencedValidator(final RpslObjectUpdateDao updateObjectDao, final RpslObjectDao objectDao) {
        this.updateObjectDao = updateObjectDao;
        this.objectDao = objectDao;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ROLE);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<CIString> originalAbuseMailbox = update.getReferenceObject().getValuesForAttribute(AttributeType.ABUSE_MAILBOX);
        final Set<CIString> updatedAbuseMailbox = update.getUpdatedObject().getValuesForAttribute(AttributeType.ABUSE_MAILBOX);

        if (!updatedAbuseMailbox.isEmpty() || originalAbuseMailbox.isEmpty()) {
            return;
        }

        for (final RpslObjectInfo referenceInfo : updateObjectDao.getReferences(update.getUpdatedObject())) {
            if (referenceInfo.getObjectType() == ObjectType.ORGANISATION) {
                final List<RpslAttribute> abuseCAttributes = objectDao.getById(referenceInfo.getObjectId()).findAttributes(AttributeType.ABUSE_C);
                if (!abuseCAttributes.isEmpty()) {
                    updateContext.addMessage(update, UpdateMessages.abuseMailboxReferenced(update.getUpdatedObject().getValueForAttribute(AttributeType.ROLE)));
                    break;
                }
            }
        }
    }
}
