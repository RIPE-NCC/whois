package net.ripe.db.whois.update.handler.validator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.MessageWithAttribute;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public interface BusinessRuleValidator {
    ImmutableList<Action> getActions();

    ImmutableList<ObjectType> getTypes();

    List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext);

    default boolean isSkipForOverride() {
        return false;
    }

    default void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        final List<Message> messages = performValidation(update, updateContext);

        final boolean isConvertErrorToWarning = isSkipForOverride() && hasOverride(update, updateContext);
        if(!isConvertErrorToWarning) {
            messages.forEach( (validationMessage) -> addMessageToContext(update, updateContext, validationMessage));
            return;
        }

        final List<Message> errorToWarningMsgs =  messages.stream().filter( message -> message.getType() == Messages.Type.ERROR).collect(Collectors.toList());

        final List<Message> remainingMsgs = Lists.newArrayList(CollectionUtils.removeAll(messages, errorToWarningMsgs));

        errorToWarningMsgs.forEach( (validationMessage)  -> addWarningToContext(update, updateContext, validationMessage));
        remainingMsgs.forEach( (validationMessage) -> addMessageToContext(update, updateContext, validationMessage));
    }

    private boolean hasOverride(final PreparedUpdate update, final UpdateContext updateContext) {
        return updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);
    }

    private void addMessageToContext(final PreparedUpdate update, final UpdateContext updateContext, final Message message) {
        if(message instanceof MessageWithAttribute) {
            updateContext.addMessage(update, ((MessageWithAttribute) message).getRpslAttribute(), message);
            return;
        }
        updateContext.addMessage(update, message);
    }

    private void addWarningToContext(final PreparedUpdate update, final UpdateContext updateContext, final  Message message) {
            final Message warningMsg =  (message instanceof MessageWithAttribute) ?
                                   new MessageWithAttribute(Messages.Type.WARNING,  ((MessageWithAttribute) message).getRpslAttribute() , message.getText(), message.getArgs())
                                :  new Message(Messages.Type.WARNING,  message.getText(), message.getArgs());
            addMessageToContext(update, updateContext, warningMsg);
    }
}
