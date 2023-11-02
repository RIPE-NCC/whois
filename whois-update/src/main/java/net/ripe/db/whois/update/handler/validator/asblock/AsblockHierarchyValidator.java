package net.ripe.db.whois.update.handler.validator.asblock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
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

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.AS_BLOCK);
    private final RpslObjectDao rpslObjectDao;

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Autowired
    public AsblockHierarchyValidator(final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final AsBlockRange asBlockNew = AsBlockRange.parse(update.getUpdatedObject().getKey().toString());
        final List<RpslObject> intersections = rpslObjectDao.findAsBlockIntersections(asBlockNew.getBegin(), asBlockNew.getEnd());
        final List<Message> messages = Lists.newArrayList();

        for (final RpslObject intersection : intersections) {
            final AsBlockRange asBlockExisting = AsBlockRange.parse(intersection.getKey().toString());
            if (asBlockExisting.equals(asBlockNew)) {
                messages.add(UpdateMessages.asblockAlreadyExists());
            } else if (asBlockExisting.contains(asBlockNew)) {
                messages.add(UpdateMessages.asblockParentAlreadyExists());
            } else if (asBlockNew.contains(asBlockExisting)) {
                messages.add(UpdateMessages.asblockChildAlreadyExists());
            } else {
                messages.add(UpdateMessages.intersectingAsblockAlreadyExists());
            }
        }

        return messages;
    }
}
