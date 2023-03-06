package net.ripe.db.nrtm4.domain;

public class DeltaFile {

    private final long id;
    private final long versionId;
    private final String name;
    private final String hash;
    private final String payload;
    private final long created;

    public DeltaFile(
        final long id,
        final long versionId,
        final String name,
        final String hash,
        final String payload,
        final long created
    ) {
        this.id = id;
        this.versionId = versionId;
        this.name = name;
        this.hash = hash;
        this.payload = payload;
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

    public String getHash() {
        return hash;
    }

    public String getPayload() {
        return payload;
    }

    public long getCreated() {
        return created;
    }

}
