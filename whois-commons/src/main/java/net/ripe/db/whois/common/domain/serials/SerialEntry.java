package net.ripe.db.whois.common.domain.serials;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class SerialEntry {
    final private Operation operation;
    final private boolean atLast;

    final private int lastTimestamp;
    final private int historyTimestamp;

    private RpslObject rpslObject;

    public SerialEntry(final Operation operation, final boolean atLast, final int lastTimestamp, final int historyTimestamp) {
        this.operation = operation;
        this.atLast = atLast;
        this.lastTimestamp = lastTimestamp;
        this.historyTimestamp = historyTimestamp;
        rpslObject = null;
    }

    public SerialEntry(final Operation operation, final boolean atLast, final int objectId, final int lastTimestamp, final int historyTimestamp, final byte[] blob) {
        this(operation, atLast, lastTimestamp, historyTimestamp);
        rpslObject = RpslObject.parse(objectId, blob);
    }

    public static SerialEntry createSerialEntryWithoutTimestamps(final Operation operation, final boolean atLast, final int objectId, final byte[] blob){
        return new SerialEntry(operation, atLast, objectId, 0, 0, blob);
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public Operation getOperation() {
        return operation;
    }

    public boolean isAtLast() {
        return atLast;
    }

    public int getLastTimestamp() {
        return lastTimestamp;
    }

    public int getHistoryTimestamp() {
        return historyTimestamp;
    }
}
