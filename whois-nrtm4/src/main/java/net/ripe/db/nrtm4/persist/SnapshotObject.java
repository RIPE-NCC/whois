package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.rpsl.ObjectType;


public class SnapshotObject {

    private final long id;
    private final long versionId;
    private final int serialId;
    private final ObjectType objectType;
    private final String pkey;
    private final String payload;

    public SnapshotObject(
        final long id,
        final long versionId,
        final int serialId,
        final ObjectType objectType,
        final String pkey,
        final String payload
    ) {
        this.id = id;
        this.versionId = versionId;
        this.serialId = serialId;
        this.objectType = objectType;
        this.pkey = pkey;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public long getVersionId() {
        return versionId;
    }

    public int getSerialId() {
        return serialId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getPkey() {
        return pkey;
    }

    public String getPayload() {
        return payload;
    }

}
