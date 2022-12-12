package net.ripe.db.nrtm4.persist;

import com.fasterxml.jackson.annotation.JsonValue;


public enum NrtmDocumentType {
    DELTA,
    SNAPSHOT,
    NOTIFICATION;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
