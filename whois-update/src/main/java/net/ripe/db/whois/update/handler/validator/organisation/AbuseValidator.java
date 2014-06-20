package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
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

import java.util.List;

import static net.ripe.db.whois.common.collect.CollectionHelper.uniqueResult;

@Component
public class AbuseValidator implements BusinessRuleValidator {

    private final RpslObjectDao objectDao;

    @Autowired
    public AbuseValidator(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ORGANISATION);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (null == updatedObject) {
            return;
        }

        validateRemovedAbuseC(updatedObject, update, updateContext);

        if (!updatedObject.containsAttribute(AttributeType.ABUSE_C)) {
            return;
        }

        final CIString abuseC = updatedObject.getValueForAttribute(AttributeType.ABUSE_C);
        final RpslObject referencedRole = uniqueResult(objectDao.getByKeys(ObjectType.ROLE, Lists.newArrayList(abuseC)));

        if (referencedRole == null) {
            if (null != uniqueResult(objectDao.getByKeys(ObjectType.PERSON, Lists.newArrayList(abuseC)))) {
                updateContext.addMessage(update, UpdateMessages.abuseCPersonReference());
            }
        } else if (!referencedRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
            updateContext.addMessage(update, UpdateMessages.abuseMailboxRequired(abuseC));
        }
    }

    private void validateRemovedAbuseC(final RpslObject updatedObject, final PreparedUpdate update, final UpdateContext updateContext) {
        final OrgType orgType = OrgType.getFor(updatedObject.getValueForAttribute(AttributeType.ORG_TYPE));
        if (orgType == OrgType.LIR && hasRemovedAbuseC(updatedObject, update)) {
            updateContext.addMessage(update, UpdateMessages.abuseContactNotRemovable()); //TODO correct errormessage
        }
    }

    private boolean hasRemovedAbuseC(final RpslObject updatedObject, final PreparedUpdate update) {
        final boolean hasAbuseC = updatedObject.containsAttribute(AttributeType.ABUSE_C);

        final RpslObject referenceObject = update.getReferenceObject();
        final boolean originalHasAbuseC = null != referenceObject && referenceObject.containsAttribute(AttributeType.ABUSE_C);

        return update.getAction() == Action.MODIFY && !hasAbuseC && originalHasAbuseC;
    }
}
