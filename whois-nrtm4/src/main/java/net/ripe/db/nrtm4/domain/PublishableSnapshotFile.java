package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PublishableSnapshotFile extends PublishableNrtmDocument {

    @JsonIgnore
    private String fileName;
    @JsonIgnore
    private String sha256hex;

    public PublishableSnapshotFile(
        final NrtmVersionInfo version
    ) {
        super(version);
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setHash(final String sha256hex) {
        this.sha256hex = sha256hex;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHash() {
        return sha256hex;
    }

}
