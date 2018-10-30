package net.ripe.db.whois.update.domain;

public enum UpdateStatus {
    EXCEPTION("FAILED"),
    FAILED_AUTHENTICATION("FAILED"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS");

    private final String status;

    UpdateStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
