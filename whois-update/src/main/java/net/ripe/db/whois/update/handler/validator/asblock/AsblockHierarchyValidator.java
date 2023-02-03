package net.ripe.db.whois.update.handler.validator.asblock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
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
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final AsBlockRange asBlockNew = AsBlockRange.parse(update.getUpdatedObject().getKey().toString());
        final List<RpslObject> intersections = rpslObjectDao.findAsBlockIntersections(asBlockNew.getBegin(), asBlockNew.getEnd());
        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();

        for (final RpslObject intersection : intersections) {
            final AsBlockRange asBlockExisting = AsBlockRange.parse(intersection.getKey().toString());
            if (asBlockExisting.equals(asBlockNew)) {
                customValidationMessages.add( new CustomValidationMessage(UpdateMessages.asblockAlreadyExists()));
            } else if (asBlockExisting.contains(asBlockNew)) {
                customValidationMessages.add( new CustomValidationMessage(UpdateMessages.asblockParentAlreadyExists()));
            } else if (asBlockNew.contains(asBlockExisting)) {
                customValidationMessages.add( new CustomValidationMessage(UpdateMessages.asblockChildAlreadyExists()));
            } else {
                customValidationMessages.add( new CustomValidationMessage(UpdateMessages.intersectingAsblockAlreadyExists()));
            }
        }

        return customValidationMessages;
    }

    @Override
    public boolean isSkipForOverride() {
        return false;
    }
}
