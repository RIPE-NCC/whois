package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import java.util.Optional;

/**
 * Do not allow 'bogon' space to be used when creating a route(6) object.
 *
 * Ref. http://www.team-cymru.com/bogon-reference.html
 */
@Component
public class BogonValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);

    private final ReservedResources reservedResources;
    @Autowired
    public BogonValidator(final ReservedResources reservedResources) {
        this.reservedResources = reservedResources;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return Collections.emptyList();
        }

        final List<Message> customValidationMessages = Lists.newArrayList();
        getPrefix(updatedObject).ifPresent(prefix -> {
            if (reservedResources.isBogon(prefix.toString())) {
                customValidationMessages.add(UpdateMessages.bogonPrefixNotAllowed(prefix.toString()));
            }
        });

        return customValidationMessages;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    private Optional<CIString> getPrefix(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case ROUTE:
                return Optional.of(rpslObject.getValueForAttribute(AttributeType.ROUTE));
            case ROUTE6:
                return Optional.of(rpslObject.getValueForAttribute(AttributeType.ROUTE6));
            default:
                return Optional.empty();
        }
    }

}
