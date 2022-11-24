package net.ripe.db.nrtm4.persist;

public class DeltaFileModel {

    private final long id;
    private final long versionId;
    private final String name;
    private final String payload;
    private final String hash;
    private final long created;

    public DeltaFileModel(
        final long id,
        final long versionId,
        final String name,
        final String payload,
        final String hash,
        final long created
    ) {
        this.id = id;
        this.versionId = versionId;
        this.name = name;
        this.payload = payload;
        this.hash = hash;
        this.created = created;
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

    public String getPayload() {
        return payload;
    }

    public String getHash() {
        return hash;
    }

    public long getCreated() {
        return created;
    }

}
