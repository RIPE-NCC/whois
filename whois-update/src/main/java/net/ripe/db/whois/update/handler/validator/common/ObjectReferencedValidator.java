package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import jakarta.ws.rs.HEAD;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if (!update.hasOriginalObject() || update.getType().equals(ObjectType.AUT_NUM)) {
            return Collections.emptyList();
        }

        if (referencesDao.isReferenced(update.getReferenceObject())) {
            return Arrays.asList(UpdateMessages.objectInUse(update.getReferenceObject()));
        }

        return Collections.emptyList();
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
