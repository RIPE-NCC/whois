package net.ripe.db.whois.api.transfer;

import javax.ws.rs.core.Response;

public class TransferFailedException extends RuntimeException {
    private final Response.Status status;

    public TransferFailedException(final Response.Status status, final String msg) {
        super(msg);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }

}
