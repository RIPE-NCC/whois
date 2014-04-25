package net.ripe.db.whois.api.rest.client;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import org.apache.commons.lang.StringUtils;

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

    public RestClientException(final Throwable cause) {
        super(cause);
        this.errorMessages = Collections.singletonList(
                new ErrorMessage(new Message(Messages.Type.ERROR, cause.getMessage())));
    }


    @Override
    public String toString() {
        return StringUtils.join(errorMessages, '\n');
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
