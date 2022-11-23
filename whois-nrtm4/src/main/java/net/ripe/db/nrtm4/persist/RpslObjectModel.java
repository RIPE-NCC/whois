package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;


public class RpslObjectModel {

    final long objectId;
    final long sequenceId;
    final long timestamp;
    final ObjectType objectType;
    final RpslObject object;
    final String pkey;

    public RpslObjectModel(
        final long objectId,
        final long sequenceId,
        final long timestamp,
        final ObjectType objectType,
        final RpslObject object,
        final String pkey
    ) {
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.timestamp = timestamp;
        this.objectType = objectType;
        this.object = object;
        this.pkey = pkey;
    }

    public long getObjectId() {
        return objectId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public RpslObject getObject() {
        return object;
    }

    public String getPkey() {
        return pkey;
    }

}
