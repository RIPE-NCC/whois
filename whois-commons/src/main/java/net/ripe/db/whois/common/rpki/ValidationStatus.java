package net.ripe.db.whois.common.rpki;

public enum ValidationStatus {

    NOT_FOUND,
    VALID,
    INVALID,
    INVALID_ORIGIN,
    INVALID_PREFIX_LENGTH
}
