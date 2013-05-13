package net.ripe.db.whois.update.handler.validator.asblock;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
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
public class AsblockHierarchyValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> actions = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> types = ImmutableList.of(ObjectType.AS_BLOCK);
    private final RpslObjectDao rpslObjectDao;

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public List<ObjectType> getTypes() {
        return types;
    }

    @Autowired
    public AsblockHierarchyValidator(final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        AsBlockRange asBlockNew = AsBlockRange.parse(update.getUpdatedObject().getKey().toString());
        final List<RpslObject> intersections = rpslObjectDao.findAsBlockIntersections(asBlockNew.getBegin(), asBlockNew.getEnd());

        for (final RpslObject intersection : intersections) {
            final AsBlockRange asBlockExisting = AsBlockRange.parse(intersection.getKey().toString());
            if (asBlockExisting.equals(asBlockNew)) {
                updateContext.addMessage(update, UpdateMessages.asblockAlreadyExists());
            } else if (asBlockExisting.contains(asBlockNew)) {
                updateContext.addMessage(update, UpdateMessages.asblockParentAlreadyExists());
            } else if (asBlockNew.contains(asBlockExisting)) {
                updateContext.addMessage(update, UpdateMessages.asblockChildAlreadyExists());
            } else {
                updateContext.addMessage(update, UpdateMessages.intersectingAsblockAlreadyExists());
            }
        }
    }
}
