package net.ripe.db.whois.common.rpsl.attrs;

public class AttributeParseException extends IllegalArgumentException {
    public AttributeParseException(final String message, final String value) {
        super(message + " (" + value + ")");
    }
}
