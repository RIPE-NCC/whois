package net.ripe.db.rndreference;

import org.joda.time.DateTime;

public class HistoricRpslObject extends DatabaseRpslObject {
    private byte[] objectBytes;

    public HistoricRpslObject(final DateTime dateTime, final byte[] bytes) {
        super(dateTime);
        this.objectBytes = bytes;
    }

    public byte[] getObjectBytes() {
        return objectBytes;
    }
}
