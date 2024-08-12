package net.ripe.db.whois.common.oauth.filter;

public enum AuthorityPrefix {
    USER,
    APP,
    SCOPE;

    public String getValue() {
        return name() + "_";
    }
}
