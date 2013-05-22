package net.ripe.db.whois.common.source;

public final class IllegalSourceException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final String source;

    public IllegalSourceException(final String source) {
        super(String.format("Invalid source specified: %s", source));
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
