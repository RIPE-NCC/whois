package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractPasswordsValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);

    private final ImmutableList<ObjectType> types;

    private final boolean isPasswordSupported;

    AbstractPasswordsValidator(final boolean isPasswordSupported, final ImmutableList<ObjectType> types){
        this.isPasswordSupported = isPasswordSupported;
        this.types = types;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {

        if (isPasswordSupported || update.getUpdatedObject() == null){
            return Collections.emptyList();
        }

        final Set<CIString> attributes = update.getUpdatedObject().getValuesForAttribute(AttributeType.AUTH);
        if (hasPassword(attributes)){
            return List.of(UpdateMessages.passwordsNotSupported());
        }

        return Collections.emptyList();
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return types;
    }

    private boolean hasPassword(final Set<CIString> attributes){
        return attributes.stream().anyMatch(credential -> credential.contains("MD5-PW"));
    }
}
