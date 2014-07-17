package net.ripe.db.whois.internal.api.rnd.domain;

import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ObjectReference {

    private final ObjectType fromObjectType;
    private final String fromPkey;
    private final int fromObjectId;
    private final int fromSequenceId;
    private final ObjectType toObjectType;
    private final String toPkey;
    private final int toObjectId;
    private final int toSequenceId;
    private final long fromTimestamp;
    private final long toTimestamp;

    public ObjectReference(final ObjectType fromObjectType, final String fromPkey, final int fromObjectId, final int fromSequenceId, final ObjectType toObjectType, final String toPkey, final int toObjectId, final int toSequenceId, final long fromTimestamp, final long toTimestamp) {
        this.fromObjectType = fromObjectType;
        this.fromPkey = fromPkey;
        this.fromObjectId = fromObjectId;
        this.fromSequenceId = fromSequenceId;
        this.toObjectType = toObjectType;
        this.toPkey = toPkey;
        this.toObjectId = toObjectId;
        this.toSequenceId = toSequenceId;
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
    }

    public ObjectType getFromObjectType() {
        return fromObjectType;
    }

    public String getFromPkey() {
        return fromPkey;
    }

    public int getFromObjectId() {
        return fromObjectId;
    }

    public int getFromSequenceId() {
        return fromSequenceId;
    }

    public ObjectType getToObjectType() {
        return toObjectType;
    }

    public String getToPkey() {
        return toPkey;
    }

    public int getToObjectId() {
        return toObjectId;
    }

    public int getToSequenceId() {
        return toSequenceId;
    }

    public long getFromTimestamp() {
        return fromTimestamp;
    }

    public long getToTimestamp() {
        return toTimestamp;
    }


}
