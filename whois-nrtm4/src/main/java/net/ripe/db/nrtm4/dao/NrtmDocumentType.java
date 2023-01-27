package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.annotation.JsonValue;


public enum NrtmDocumentType {
    DELTA("nrtm-delta"),
    SNAPSHOT("nrtm-snapshot"),
    NOTIFICATION("nrtm-notification");

    private final String fileNamePrefix;

    NrtmDocumentType(final String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    @JsonValue
    public String lowerCaseName() {
        return name().toLowerCase();
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

}
