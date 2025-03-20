package net.ripe.db.whois.api.rdap.domain;

public enum LinkRelationType {
    UP("rdap-up"),
    TOP("rdap-top"),
    DOWN("rdap-down"),
    BOTTOM("rdap-bottom"),
    ACTIVE("rdap-active");

    private final String value;

    LinkRelationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getActiveLinkRelation(final LinkRelationType type) {
        return type.value.concat(" ").concat(ACTIVE.getValue());
    }

    public static boolean containsValidValue(final String value) {
        for (RelationType type : RelationType.values()) {
            if (value.contains(type.getValue())) {
                return true;
            }
        }
        return false;
    }
}
