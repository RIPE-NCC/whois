package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;


public class RpslObjectModel extends RpslObjectUpdateInfo {

    private final RpslObject object;
    private final long timestamp;

    public RpslObjectModel(
        final int objectId,
        final int sequenceId,
        final ObjectType objectType,
        final String key,
        final RpslObject object,
        final long timestamp
    ) {
        super(objectId, sequenceId, objectType, key);
        this.object = object;
        this.timestamp = timestamp;
    }

    public RpslObject getObject() {
        return object;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
