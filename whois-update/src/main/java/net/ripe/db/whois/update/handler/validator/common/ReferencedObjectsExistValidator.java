package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
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

    private final RpslObjectUpdateDao rpslObjectUpdateDao;

    @Autowired
    public ReferencedObjectsExistValidator(final RpslObjectUpdateDao rpslObjectUpdateDao) {
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final Map<RpslAttribute, Set<CIString>> invalidReferences = rpslObjectUpdateDao.getInvalidReferences(updatedObject);
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
