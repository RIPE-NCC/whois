package net.ripe.db.whois.api.transfer;

import net.ripe.db.whois.api.rest.domain.WhoisResources;

import javax.ws.rs.core.Response;

public class TransferFailedException extends RuntimeException {
    private final WhoisResources whoisResources;
    private final Response.Status status;

    public TransferFailedException(final Response.Status status, final WhoisResources whoisResources) {
        this.status = status;
        this.whoisResources = whoisResources;
    }

    public Response.Status getStatus() {
        return status;
    }

    public WhoisResources getWhoisResources() {
        return whoisResources;
    }


}
