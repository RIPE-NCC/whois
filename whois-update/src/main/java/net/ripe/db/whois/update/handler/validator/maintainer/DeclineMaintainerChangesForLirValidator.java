package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeclineMaintainerChangesForLirValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);


    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public DeclineMaintainerChangesForLirValidator(final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        CIString orgKey = updatedObject.getValueForAttribute(AttributeType.ORG);
        if (orgKey == null) {
            return;
        }

        RpslObject organisation = rpslObjectDao.getByKey(ObjectType.ORGANISATION, orgKey);
        if (organisation == null) {
            updateContext.addMessage(update, UpdateMessages.referenceNotFound(orgKey));
            return;
        }

        if (OrgType.getFor(organisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR) {
            updateContext.addMessage(update, UpdateMessages.declineMaintainerChangesForLir(updatedObject.getKey().toString()));
            return;
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
