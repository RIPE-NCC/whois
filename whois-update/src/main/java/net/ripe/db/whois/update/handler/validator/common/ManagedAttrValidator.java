package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.search.ManagedAttributeSearch;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        final RpslObject updatedObject = update.getUpdatedObject();
        for(RpslAttribute attribute :updatedObject.getAttributes()) {

            if(managedAttributeSearch.isRipeNccMaintained(updatedObject,attribute) && attribute.getCleanComment() != null) {
                messages.add(UpdateMessages.canNotAddCommentsInManagedAttr(attribute));
            }
        }

        return messages;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
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
