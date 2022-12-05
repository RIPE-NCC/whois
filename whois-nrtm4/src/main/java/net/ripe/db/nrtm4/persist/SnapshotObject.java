package net.ripe.db.nrtm4.persist;

public class SnapshotObject {

    private final long id;
    private final int serialId;
    private final String payload;

    public SnapshotObject(
        final long id,
        final int serialId,
        final String payload) {

        this.id = id;
        this.serialId = serialId;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public int getSerialId() {
        return serialId;
    }

    public String getPayload() {
        return payload;
    }

}
