package net.ripe.db.whois.common.sso;

public class AuthServiceClientException extends RuntimeException {

    private final String message;
    private final int code;

    public AuthServiceClientException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
