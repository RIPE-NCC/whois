package net.ripe.db.nrtm4.client.dao;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public enum NrtmClientDocumentType {
    SNAPSHOT("nrtm-snapshot"),
    DELTA("nrtm-delta"),
    NOTIFICATION("update-notification-file");

    private final String fileNamePrefix;

    NrtmClientDocumentType(final String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    @Nullable
    public static NrtmClientDocumentType fromValue(String value) {
        if (StringUtils.isEmpty(value)){
            return null;
        }

        for (NrtmClientDocumentType enumConstant : NrtmClientDocumentType.values()) {
            if (enumConstant.getFileNamePrefix().equals(value)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}
