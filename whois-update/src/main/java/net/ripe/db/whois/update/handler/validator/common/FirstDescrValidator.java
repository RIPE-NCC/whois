package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirstDescrValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final Maintainers maintainers;

    @Autowired
    public FirstDescrValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
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
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.POWER_MAINTAINER)) {
            return;
        }

        if (!mntbyPowerMaintainer(update.getReferenceObject())) {
            return;
        }

        if (update.getReferenceObject().containsAttribute(AttributeType.DESCR)) {
            if (!update.getUpdatedObject().containsAttribute(AttributeType.DESCR)) {
                updateContext.addMessage(update, UpdateMessages.descrCannotBeRemoved());
            } else {
                if (!getFirstDescr(update.getReferenceObject()).equals(getFirstDescr(update.getUpdatedObject()))) {
                    updateContext.addMessage(update, UpdateMessages.descrCannotBeChanged());
                }
            }
        } else {
            if (update.getUpdatedObject().containsAttribute(AttributeType.DESCR)) {
                    updateContext.addMessage(update, UpdateMessages.descrCannotBeAdded());
            }
        }
    }

    private boolean mntbyPowerMaintainer(final RpslObject rpslObject) {
        return !Sets.intersection(maintainers.getPowerMaintainers(), rpslObject.getValuesForAttribute(AttributeType.MNT_BY)).isEmpty();
    }

    private CIString getFirstDescr(final RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(AttributeType.DESCR)) {
            throw new IllegalArgumentException("no descr");
        }

        return rpslObject.findAttributes(AttributeType.DESCR).get(0).getCleanValue();
    }
}
