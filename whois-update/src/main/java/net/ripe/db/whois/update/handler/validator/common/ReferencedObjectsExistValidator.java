package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ReferencedObjectsExistValidator implements BusinessRuleValidator {
    private final RpslObjectUpdateDao rpslObjectUpdateDao;

    @Autowired
    public ReferencedObjectsExistValidator(final RpslObjectUpdateDao rpslObjectUpdateDao) {
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final Map<RpslAttribute, Set<String>> invalidReferences = rpslObjectUpdateDao.getInvalidReferences(updatedObject);
        final ObjectMessages objectMessages = updateContext.getMessages(update);
        for (final Map.Entry<RpslAttribute, Set<String>> invalidReferenceEntry : invalidReferences.entrySet()) {
            final RpslAttribute attribute = invalidReferenceEntry.getKey();
            if (objectMessages.getMessages(attribute).getErrors().isEmpty() && !objectMessages.contains(UpdateMessages.unknownObjectReferenced(StringUtils.join(invalidReferenceEntry.getValue(), ',')))) {
                updateContext.addMessage(update, attribute, UpdateMessages.unknownObjectReferenced(StringUtils.join(invalidReferenceEntry.getValue(), ',')));
            }
        }
    }
}
