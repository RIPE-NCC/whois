package net.ripe.db.whois.common.dao;

public class VersionVanishedException extends IllegalStateException {
    public VersionVanishedException(String s) {
        super(s);
    }

    public VersionVanishedException(String message, Throwable cause) {
        super(message, cause);
    }
}
