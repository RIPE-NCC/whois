package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PublishableNrtmFile extends PublishableNrtmDocument {

    @JsonIgnore
    private String fileName;
    @JsonIgnore
    private String hash;

    protected PublishableNrtmFile() {}

    public PublishableNrtmFile(
        final NrtmVersionInfo version
    ) {
        super(version);
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setHash(final String sha256hex) {
        this.hash = sha256hex;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHash() {
        return hash;
    }

}
