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

    public static RelationType fromString(final String value) {
        for (RelationType type : RelationType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
