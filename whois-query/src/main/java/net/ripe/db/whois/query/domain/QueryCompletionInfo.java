package net.ripe.db.whois.query.domain;

public enum QueryCompletionInfo {
    BLOCKED(true),
    DISCONNECTED(true),
    PARAMETER_ERROR(false),
    PROXY_NOT_ALLOWED(true),
    UNSUPPORTED_QUERY(false),
    REJECTED(true),
    EXCEPTION(true);

    private boolean forceClose;

    private QueryCompletionInfo(final boolean forceClose) {
        this.forceClose = forceClose;
    }

    public boolean isForceClose() {
        return forceClose;
    }
}
