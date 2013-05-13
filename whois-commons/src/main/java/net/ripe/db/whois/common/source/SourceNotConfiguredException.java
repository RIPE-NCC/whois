package net.ripe.db.whois.common.source;

public final class SourceNotConfiguredException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String source;

    public SourceNotConfiguredException(final String source) {
        super(String.format("Invalid source specified: %s", source));
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
