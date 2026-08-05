package net.ripe.db.whois.api.security.auth;

public class AccessTokenValidationException extends RuntimeException {

    private final String message;
    private final int code;

    public AccessTokenValidationException(final String message) {
        this.code = 401;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
