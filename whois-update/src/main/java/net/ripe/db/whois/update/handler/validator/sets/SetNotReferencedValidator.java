package net.ripe.db.whois.update.handler.validator.sets;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class SetNotReferencedValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.AS_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET);

    private final RpslObjectDao objectDao;

    @Autowired
    public SetNotReferencedValidator(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final List<RpslObjectInfo> incomingReferences = objectDao.findMemberOfByObjectTypeWithoutMbrsByRef(updatedObject.getType(), updatedObject.getKey().toString());
        if (!incomingReferences.isEmpty()) {
            return Arrays.asList(UpdateMessages.objectInUse(updatedObject));
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
