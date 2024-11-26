package net.ripe.db.nrtm4.client.dao;

import io.netty.util.internal.StringUtil;

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

    public static NrtmClientDocumentType fromValue(String value) {
        if (StringUtil.isNullOrEmpty(value)){
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
