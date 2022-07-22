package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ReferencedObjectsExistValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private final ReferencesDao referencesDao;

    @Autowired
    public ReferencedObjectsExistValidator(final ReferencesDao referencesDao) {
        this.referencesDao = referencesDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final Map<RpslAttribute, Set<CIString>> invalidReferences = referencesDao.getInvalidReferences(updatedObject);
        final ObjectMessages objectMessages = updateContext.getMessages(update);
        for (final Map.Entry<RpslAttribute, Set<CIString>> invalidReferenceEntry : invalidReferences.entrySet()) {
            final RpslAttribute attribute = invalidReferenceEntry.getKey();
            if (objectMessages.getMessages(attribute).getErrors().isEmpty()) {
                updateContext.addMessage(update, attribute, UpdateMessages.unknownObjectReferenced(StringUtils.join(invalidReferenceEntry.getValue(), ',')));
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
