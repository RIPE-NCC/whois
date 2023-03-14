package net.ripe.db.nrtm4.domain;

import java.util.List;


public class PublishableSnapshotFile extends PublishableNrtmFile {

    private List<String> objects;

    private PublishableSnapshotFile() {}

    public PublishableSnapshotFile(final NrtmVersionInfo nextVersion) {
        super(nextVersion);
        this.objects = null; // explicitly serialized by SnapshotSerializer
    }

    public List<String> getObjects() {
        return objects;
    }

}
