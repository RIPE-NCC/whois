package net.ripe.db.whois.api.rdap.domain;

import net.ripe.db.whois.api.rdap.RdapException;
import org.eclipse.jetty.http.HttpStatus;

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

    public static RelationType fromValue(final String value) {
        for (RelationType type : RelationType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new RdapException("400 Bad Request", "Relation " + value + " doesn't exist", HttpStatus.BAD_REQUEST_400);
    }

    public static boolean isValidValue(final String value) {
        for (RelationType type : RelationType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
