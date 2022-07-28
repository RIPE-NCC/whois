package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObjectReferencedValidator implements BusinessRuleValidator {
    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private final ReferencesDao referencesDao;

    @Autowired
    public ObjectReferencedValidator(final ReferencesDao referencesDao) {
        this.referencesDao = referencesDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (!update.hasOriginalObject() || update.getType().equals(ObjectType.AUT_NUM)) {
            return;
        }

        if (referencesDao.isReferenced(update.getReferenceObject())) {
            updateContext.addMessage(update, UpdateMessages.objectInUse(update.getReferenceObject()));
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
