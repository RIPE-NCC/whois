package net.ripe.db.nrtm4.client.dao;

public enum NrtmClientDocumentType {
    SNAPSHOT("nrtm-snapshot"),
    NOTIFICATION("update-notification-file");

    private final String fileNamePrefix;

    NrtmClientDocumentType(final String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public static NrtmClientDocumentType fromValue(String value) {
        for (NrtmClientDocumentType enumConstant : NrtmClientDocumentType.values()) {
            if (enumConstant.getFileNamePrefix().equals(value)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}
