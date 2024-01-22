package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.ReservedResources;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OriginValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);

    private final RpslObjectDao rpslObjectDao;
    private final AuthoritativeResourceData authoritativeResourceData;
    private final ReservedResources reservedResources;

    @Autowired
    public OriginValidator(final RpslObjectDao rpslObjectDao, final ReservedResources reservedResources, final AuthoritativeResourceData authoritativeResourceData) {
        this.rpslObjectDao = rpslObjectDao;
        this.authoritativeResourceData = authoritativeResourceData;
        this.reservedResources = reservedResources;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
           return Collections.emptyList();
        }

        final CIString autnumKey = updatedObject.getValueForAttribute(AttributeType.ORIGIN);
        AutNum autnum = AutNum.parse(autnumKey);

        final List<Message> customValidationMessage = Lists.newArrayList();
        if (reservedResources.isReservedAsNumber(autnum.getValue())) {
            customValidationMessage.add(UpdateMessages.cannotUseReservedAsNumber(autnum.getValue()));
        } else if (authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(ObjectType.AUT_NUM, autnumKey) &&
                   rpslObjectDao.findByKeyOrNull(ObjectType.AUT_NUM, autnumKey) == null) {
            customValidationMessage.add(UpdateMessages.autnumNotFoundInDatabase(autnum.getValue()));
        }

        return customValidationMessage;
    }
}
