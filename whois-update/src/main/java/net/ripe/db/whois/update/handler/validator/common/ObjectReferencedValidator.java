package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObjectReferencedValidator implements BusinessRuleValidator {
    private final RpslObjectUpdateDao rpslObjectUpdateDao;

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Autowired
    public ObjectReferencedValidator(final RpslObjectUpdateDao rpslObjectUpdateDao) {
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (!update.hasOriginalObject() || update.getType().equals(ObjectType.AUT_NUM)) {
            return;
        }

        if (rpslObjectUpdateDao.isReferenced(update.getReferenceObject())) {
            updateContext.addMessage(update, UpdateMessages.objectInUse(update.getReferenceObject()));
        }
    }
}
