package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.util.Objects;


public class ObjectData implements Comparable<ObjectData> {
    private final RpslObjectKey key;
    private final long timestamp;
    private final int objectId;
    private final int sequenceId;

    public ObjectData(final CIString pkey, final ObjectType objectType, final long timestamp, final int objectId, final int sequenceId) {
        this.key = new RpslObjectKey(pkey, objectType);
        this.timestamp = timestamp;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
    }

    public ObjectData(final String pkey, final ObjectType objectType, final long timestamp, final int objectId, final int sequenceId) {
        this(CIString.ciString(pkey), objectType, timestamp, objectId, sequenceId);
    }

    public RpslObjectKey getKey() {
        return key;
    }

    public CIString getPrimaryKey() {
        return key.getPrimaryKey();
    }

    public ObjectType getType() {
        return key.getObjectType();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    @Override
    public int compareTo(final ObjectData o) {
        final int c1 = Integer.compare(objectId, o.objectId);
        if (c1 != 0) {
            // order by objectId
            return c1;
        }

        final int c2 = Long.compare(timestamp, o.timestamp);
        if (c2 != 0) {
            // order by date
            return c2;
        }

        // order by sequenceId (delete last)
        // order by sequenceId (ascending)

        return (sequenceId == 0) ? 1 : (o.sequenceId == 0) ? -1 :
            Integer.compare(sequenceId, o.sequenceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final ObjectData that)) return false;
        return Objects.equals(key, that.key) &&
            Objects.equals(timestamp, that.timestamp) &&
            Objects.equals(objectId, that.objectId) &&
            Objects.equals(sequenceId, that.sequenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, timestamp, objectId, sequenceId);
    }
}
