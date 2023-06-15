package net.ripe.db.whois.update.handler.validator.outofregion;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
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

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class OutOfRegionObjectValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(AUT_NUM, ROUTE, ROUTE6);

    private final AuthoritativeResourceData authoritativeResourceData;

    @Autowired
    public OutOfRegionObjectValidator(final AuthoritativeResourceData authoritativeResourceData) {
        this.authoritativeResourceData = authoritativeResourceData;
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
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER) || updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return Collections.emptyList();
        }

        boolean outOfRegion = updatedObject.getType() == ROUTE || updatedObject.getType() == ROUTE6?
                !authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(updatedObject) :
                !authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(updatedObject);

        return outOfRegion ?
                  Arrays.asList(UpdateMessages.cannotCreateOutOfRegionObject(updatedObject.getType()))
                : Collections.emptyList();
    }
}
