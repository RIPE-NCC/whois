package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.search.ManagedAttributeSearch;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
public class ManagedAttrValidator implements BusinessRuleValidator {
    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY, Action.NOOP);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION, ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);
    private final ManagedAttributeSearch managedAttributeSearch;

    @Autowired
    public ManagedAttrValidator(final ManagedAttributeSearch managedAttributeSearch) {
        this.managedAttributeSearch = managedAttributeSearch;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        for(RpslAttribute attribute :updatedObject.getAttributes()) {

            if(managedAttributeSearch.isRipeNccMaintained(updatedObject,attribute) && attribute.getCleanComment() != null) {
                updateContext.addMessage(update, attribute, UpdateMessages.canNotAddCommentsInManagedAttr(attribute.getType()));
            }
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
