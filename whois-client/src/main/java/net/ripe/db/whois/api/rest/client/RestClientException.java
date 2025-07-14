package net.ripe.db.whois.api.rest.client;

import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class RestClientException extends RuntimeException {
    private int status = 0;
    private List<ErrorMessage> errorMessages;

    public RestClientException(final int status, final List<ErrorMessage> errorMessages) {
        this.status = status;
        this.errorMessages = errorMessages;
    }

    public RestClientException(final int status, @Nullable final String message) {
        this.status = status;
        this.errorMessages = Collections.singletonList(
                new ErrorMessage(new Message(Messages.Type.ERROR, message != null ? message : "no message")));
    }

    public RestClientException(@Nullable final Throwable cause) {
        super(cause);
        this.errorMessages = Collections.singletonList(
                new ErrorMessage(new Message(Messages.Type.ERROR, cause != null ? cause.getMessage() : "no cause")));
    }

    public RestClientException(final int status, @Nullable final Throwable cause) {
        super(cause);
        this.status = status;
        this.errorMessages = Collections.singletonList(
                new ErrorMessage(new Message(Messages.Type.ERROR, cause != null ? cause.getMessage() : "no cause")));
    }

    public RestClientException(final Response.Status status, final Throwable cause) {
        this(status.getStatusCode(), cause);
    }

    @Override
    public String toString() {
        return "status:" + status + ", errorMessages:" + StringUtils.join(errorMessages, '\n');
    }

    public int getStatus() {
        return status;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
