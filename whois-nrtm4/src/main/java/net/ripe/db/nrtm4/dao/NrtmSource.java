package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.annotation.JsonValue;


public class NrtmSource {

    private final String value;

    public NrtmSource(final String value) {
        this.value = value;
    }

    @JsonValue
    public String name() {
        return value;
    }

}
