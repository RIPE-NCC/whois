package net.ripe.db.whois.api.rdap.domain;

public enum RelationType {
    UP("up"),
    TOP("top"),
    DOWN("down"),
    BOTTOM("bottom");

    private final String value;

    RelationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
