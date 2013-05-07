package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.domain.VersionDateTime;

public class VersionInfo implements Identifiable, Comparable<VersionInfo> {
    private final boolean inLast;
    private final int objectId;
    private final int sequenceId;
    private final VersionDateTime timestamp;
    private final Operation operation;

    private final ObjectType objectType;
    private final String objectKey;

    public VersionInfo(final boolean inLast, final Integer objectId, final Integer sequenceId, final Long timestamp, final Operation operation, final ObjectType objectType, final String key) {
        this.inLast = inLast;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.timestamp = new VersionDateTime(timestamp);
        this.operation = operation;
        this.objectType = objectType;
        this.objectKey = key;
    }

    public boolean isInLast() {
        return inLast;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public VersionDateTime getTimestamp() {
        return timestamp;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getKey() {
        return objectKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionInfo that = (VersionInfo) o;

        return inLast == that.inLast
                && objectId == that.objectId
                && sequenceId == that.sequenceId
                && objectKey.equals(that.objectKey)
                && objectType == that.objectType
                && operation == that.operation
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = (inLast ? 1 : 0);
        result = 31 * result + objectId;
        result = 31 * result + sequenceId;
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + objectType.hashCode();
        result = 31 * result + objectKey.hashCode();
        return result;
    }

    @Override
    public int compareTo(final VersionInfo o) {
        final int result = ObjectTemplate.getTemplate(objectType).compareTo(ObjectTemplate.getTemplate(o.getObjectType()));
        if (result != 0) {
            return result;
        }

        int cmp = objectKey.compareTo(o.objectKey);
        if (cmp != 0) {
            return cmp;
        }

        return this.sequenceId - o.sequenceId;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "inLast=" + inLast +
                ", objectId=" + objectId +
                ", sequenceId=" + sequenceId +
                ", timestamp=" + timestamp +
                ", operation=" + operation +
                ", objectType=" + objectType +
                ", objectKey='" + objectKey + '\'' +
                '}';
    }
}
