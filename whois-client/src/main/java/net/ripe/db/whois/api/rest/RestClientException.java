package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;

import java.util.Collections;
import java.util.List;

public class RestClientException extends RuntimeException {

    private List<ErrorMessage> errorMessages;

    public RestClientException(final List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public RestClientException(final String message) {
        this.errorMessages = Collections.singletonList(
                new ErrorMessage(new Message(Messages.Type.ERROR, message)));
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
