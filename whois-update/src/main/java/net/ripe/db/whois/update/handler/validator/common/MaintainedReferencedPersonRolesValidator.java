package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MaintainedReferencedPersonRolesValidator extends AbstractObjectIsMaintainedValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    @Autowired
    public MaintainedReferencedPersonRolesValidator(final RpslObjectDao rpslObjectDao) {
        super(rpslObjectDao);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        for (final RpslObject rpslObject : validateReferencedPersonsAndRoles(update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.referencedObjectMissingAttribute(rpslObject.getType(), rpslObject.getKey(), AttributeType.MNT_BY));
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
