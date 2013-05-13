package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
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

import java.util.List;

@Component
public class MaintainedReferencedPersonRolesValidator extends AbstractObjectIsMaintainedValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Autowired
    public MaintainedReferencedPersonRolesValidator(final RpslObjectDao rpslObjectDao) {
        super(rpslObjectDao);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        for (final RpslObject rpslObject : validateReferencedPersonsAndRoles(update.getUpdatedObject())) {
            updateContext.addMessage(update, UpdateMessages.referencedObjectMissingAttribute(rpslObject.getType(), rpslObject.getKey(), AttributeType.MNT_BY));
        }
    }
}
