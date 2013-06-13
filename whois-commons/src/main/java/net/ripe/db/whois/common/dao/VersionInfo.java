package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;

public class VersionInfo implements Identifiable, Comparable<VersionInfo> {
    private final boolean inLast;
    private final int objectId;
    private final int sequenceId;
    private final VersionDateTime timestamp;
    private final Operation operation;

    public VersionInfo(final boolean inLast, final Integer objectId, final Integer sequenceId, final Long timestamp, final Operation operation) {
        this.inLast = inLast;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.timestamp = new VersionDateTime(timestamp);
        this.operation = operation;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionInfo that = (VersionInfo) o;

        return inLast == that.inLast
                && objectId == that.objectId
                && sequenceId == that.sequenceId
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
        return result;
    }

    @Override
    public int compareTo(final VersionInfo o) {
        final int objectIdComp = this.objectId - o.objectId;
        if (objectIdComp != 0) {
            return objectIdComp;
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
                '}';
    }
}
