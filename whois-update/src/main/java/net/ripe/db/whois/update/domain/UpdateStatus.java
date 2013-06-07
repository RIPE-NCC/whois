package net.ripe.db.whois.update.domain;

public enum UpdateStatus {
    EXCEPTION("FAILED"),
    FAILED_AUTHENTICATION("FAILED"),
    PENDING_AUTHENTICATION("PENDING"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS");

    private final String status;

    private UpdateStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
