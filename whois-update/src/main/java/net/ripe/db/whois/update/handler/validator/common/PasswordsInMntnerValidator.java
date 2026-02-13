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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class PasswordsInMntnerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);

    private final boolean isPasswordSupported;

    PasswordsInMntnerValidator(@Value("${md5.password.supported:true}") final boolean isPasswordSupported){
        this.isPasswordSupported = isPasswordSupported;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getUpdatedObject() == null) {
            return Collections.emptyList();
        }

        final Set<CIString> attributes = update.getUpdatedObject().getValuesForAttribute(AttributeType.AUTH);
        if (hasPassword(attributes) && !isPasswordSupported){
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
        return TYPES;
    }

    private boolean hasPassword(final Set<CIString> attributes){
        return attributes.stream().anyMatch(credential -> credential.contains("MD5-PW"));
    }
}
