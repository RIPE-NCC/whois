package net.ripe.db.whois.update.handler.validator;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface BusinessRuleValidator {
    ImmutableList<Action> getActions();

    ImmutableList<ObjectType> getTypes();

    List<CustomValidationMessage> performValidation(PreparedUpdate update, UpdateContext updateContext);

    default boolean isSkipForOverride() {
        return false;
    }
    default void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        final List<CustomValidationMessage> customValidationMessages = performValidation(update, updateContext);

        boolean isConvertErrorToWarning = isSkipForOverride() && updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);
        if(!isConvertErrorToWarning) {
            customValidationMessages.forEach( (validationMessage) -> addMessageToContext(update, updateContext, validationMessage.getMessage(), validationMessage.getAttribute()));
            return;
        }

        final List<CustomValidationMessage> errorToWarningMsgs =  customValidationMessages.stream()
                                                                  .filter( (customMessage) -> customMessage.getMessage().getType() == Messages.Type.ERROR && customMessage.canBeWarning())
                                                                 .collect(Collectors.toList());

        final List<CustomValidationMessage> remainingMsgs = new ArrayList<>((CollectionUtils.removeAll(customValidationMessages, errorToWarningMsgs)));

        errorToWarningMsgs.forEach( (validationMessage)  -> addWarningToContext(update, updateContext, validationMessage.getMessage(), validationMessage.getAttribute()));
        remainingMsgs.forEach( (validationMessage) -> addMessageToContext(update, updateContext, validationMessage.getMessage(), validationMessage.getAttribute()));
    }

    private void addMessageToContext(final PreparedUpdate update, final UpdateContext updateContext, final Message message, final RpslAttribute attribute) {
        if(attribute == null) {
            updateContext.addMessage(update, message);
        } else {
            updateContext.addMessage(update, attribute, message);
        }
    }

    private void addWarningToContext(final PreparedUpdate update, final UpdateContext updateContext, final  Message message, final RpslAttribute attribute) {
            addMessageToContext(update, updateContext, new Message(Messages.Type.WARNING, message.getText(), message.getArgs()), attribute);
    }
}
