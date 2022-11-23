package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.domain.serials.Operation;


public class SerialModel {

    final int serialId;
    final long objectId;
    final long sequenceId;
    final boolean atlast;
    final Operation operation;

    public SerialModel(
        final int serialId,
        final long objectId,
        final long sequenceId,
        final boolean atlast,
        final Operation operation
    ) {
        this.serialId = serialId;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.atlast = atlast;
        this.operation = operation;
    }

    public int getSerialId() {
        return serialId;
    }

    public long getObjectId() {
        return objectId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public boolean isAtlast() {
        return atlast;
    }

    public Operation getOperation() {
        return operation;
    }

}
