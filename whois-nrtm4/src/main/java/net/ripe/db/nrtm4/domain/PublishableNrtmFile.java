package net.ripe.db.nrtm4.domain;

public class PublishableNrtmFile extends PublishableNrtmDocument {


    protected PublishableNrtmFile() {
    }

    public PublishableNrtmFile(
        final NrtmVersionInfo version
    ) {
        super(version);
    }

}
