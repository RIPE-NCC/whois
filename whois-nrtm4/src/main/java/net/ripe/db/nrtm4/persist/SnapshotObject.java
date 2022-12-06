package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.rpsl.ObjectType;


public class SnapshotObject {

    private final long id;
    private final int serialId;
    private final ObjectType objectType;
    private final String payload;

    public SnapshotObject(
        final long id,
        final int serialId,
        final ObjectType objectType,
        final String payload
    ) {
        this.id = id;
        this.serialId = serialId;
        this.objectType = objectType;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public int getSerialId() {
        return serialId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getPayload() {
        return payload;
    }

}
