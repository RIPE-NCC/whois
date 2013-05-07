package net.ripe.db.whois.update.handler.validator.sets;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetNotReferencedValidator implements BusinessRuleValidator {
    private final RpslObjectDao objectDao;

    @Autowired
    public SetNotReferencedValidator(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.DELETE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.AS_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final List<RpslObjectInfo> incomingReferences = objectDao.findMemberOfByObjectTypeWithoutMbrsByRef(updatedObject.getType(), updatedObject.getKey().toString());
        if (!incomingReferences.isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.objectInUse(updatedObject));
        }
    }
}
