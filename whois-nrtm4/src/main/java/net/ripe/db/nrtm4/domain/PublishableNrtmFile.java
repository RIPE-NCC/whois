package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PublishableNrtmFile extends PublishableNrtmDocument {

    @JsonIgnore
    private final String fileName;
    @JsonIgnore
    private String hash;

    protected PublishableNrtmFile() {
        this.fileName = null;
    }

    public PublishableNrtmFile(
        final NrtmVersionInfo version,
        final String fileName
    ) {
        super(version);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

}
