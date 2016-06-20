package net.ripe.db.whois.common.rpsl;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class AuthenticationException extends NestableRuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
