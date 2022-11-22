package net.ripe.db.nrtm4.persist;

public class DeltaFile {

    private final long id;
    private final long versionId;
    private final String name;
    private final String payload;
    private final String hash;
    private final int lastSerialId;
    private final long created;

    public DeltaFile(
            final long id,
            final long versionId,
            final String name,
            final String payload,
            final String hash,
            final int lastSerialId,
            final long created) {
        this.id = id;
        this.versionId = versionId;
        this.name = name;
        this.payload = payload;
        this.hash = hash;
        this.lastSerialId = lastSerialId;
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

    public int getLastSerialId() {
        return lastSerialId;
    }

    public long getCreated() {
        return created;
    }

}
