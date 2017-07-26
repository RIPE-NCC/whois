package net.ripe.db.whois.common.dao;

import com.google.common.base.MoreObjects;
import net.ripe.db.whois.common.rpsl.ObjectType;

public class RpslObjectUpdateInfo extends RpslObjectInfo {
    final int sequenceId;

    public RpslObjectUpdateInfo(final int objectId, final int sequenceId, final ObjectType objectType, final String key) {
        super(objectId, objectType, key);
        this.sequenceId = sequenceId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("objectId", getObjectId())
                .add("objectType", getObjectType())
                .add("key", getKey())
                .add("sequenceId", sequenceId)
                .toString();
    }
}
