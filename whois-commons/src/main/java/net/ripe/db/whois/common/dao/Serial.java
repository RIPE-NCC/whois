package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.serials.Operation;


public class Serial {
    protected final int serialId;
    protected final boolean inLast;
    protected final int objectId;
    protected final int sequenceId;
    protected final Operation operation;

    public Serial(final int serialId, final boolean inLast, final int objectId, final int sequenceId, final Operation operation) {
        this.serialId = serialId;
        this.inLast = inLast;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.operation = operation;
    }

    public int getSerialId() {
        return serialId;
    }

    public boolean isInLast() {
        return inLast;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public Operation getOperation() {
        return operation;
    }

}
