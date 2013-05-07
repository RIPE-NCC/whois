package net.ripe.db.whois.api.mail.dequeue;

import org.springframework.core.NestedRuntimeException;

public class ParseException extends NestedRuntimeException {
    public ParseException(final String message) {
        super(message);
    }
}
