package net.ripe.db.whois.common.dao;

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
}
