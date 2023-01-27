package net.ripe.db.nrtm4.domain;

public class SnapshotFile {

    private final long id;
    private final long versionId;
    private final String name;
    private final String hash;

    public SnapshotFile(
        final long id,
        final long versionId,
        final String name,
        final String hash
    ) {
        this.id = id;
        this.versionId = versionId;
        this.name = name;
        this.hash = hash;
    }

    public long getId() {
        return id;
    }

    public long getVersionId() {
        return versionId;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

}
