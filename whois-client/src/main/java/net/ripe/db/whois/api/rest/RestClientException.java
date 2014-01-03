package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;

import java.util.List;

public class RestClientException extends RuntimeException {

    private List<ErrorMessage> errorMessages;

    public RestClientException(final List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
