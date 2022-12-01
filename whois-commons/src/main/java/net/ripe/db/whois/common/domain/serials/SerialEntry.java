package net.ripe.db.whois.common.domain.serials;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class SerialEntry {
    private final int serialId;
    private final Operation operation;
    private final boolean atLast;

    private final int lastTimestamp;
    private final int historyTimestamp;
    private final String primaryKey;

    private RpslObject rpslObject;

    public SerialEntry(final int serialId, final Operation operation, final boolean atLast, final int lastTimestamp, final int historyTimestamp, final String pkey) {
        this.serialId = serialId;
        this.operation = operation;
        this.atLast = atLast;
        this.lastTimestamp = lastTimestamp;
        this.historyTimestamp = historyTimestamp;
        this.primaryKey = pkey;
        rpslObject = null;
    }

    public SerialEntry(final int serialId, final Operation operation, final boolean atLast, final int objectId, final int lastTimestamp, final int historyTimestamp, final byte[] blob, final String pkey) {
        this(serialId, operation, atLast, lastTimestamp, historyTimestamp, pkey);
        rpslObject = blob == null ? null : RpslObject.parse(objectId, blob);
    }

    public SerialEntry(final int serialId, final Operation operation, final boolean atLast, final int objectId, final byte[] blob, final String pkey) {
        this(serialId, operation, atLast, objectId, 0, 0, blob, pkey);
    }

    public static SerialEntry createSerialEntryWithoutTimestamps(final int serialId, final Operation operation, final boolean atLast, final int objectId, final byte[] blob, final String pkey) {
        return new SerialEntry(serialId, operation, atLast, objectId, 0, 0, blob, pkey);
    }

    public int getSerialId() {
        return serialId;
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

    public String getPrimaryKey() {
        return primaryKey;
    }

}
