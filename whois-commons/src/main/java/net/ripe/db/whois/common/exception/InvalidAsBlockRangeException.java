package net.ripe.db.whois.common.exception;

public class InvalidAsBlockRangeException extends AsBlockParseException {
    private static final long serialVersionUID = 1L;

    public InvalidAsBlockRangeException(String s) {
        super(s);
    }
}
