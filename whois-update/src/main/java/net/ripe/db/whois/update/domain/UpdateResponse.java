package net.ripe.db.whois.update.domain;

public class UpdateResponse {
    private final UpdateStatus status;
    private final String response;

    public UpdateResponse(final UpdateStatus status, final String response) {
        this.status = status;
        this.response = response;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public String getResponse() {
        return response;
    }
}
