package net.ripe.db.nrtm4.domain;

public class SnapshotWithPayload {

   final SnapshotFile snapshotFile;
   final byte[] payload;

    public SnapshotWithPayload(SnapshotFile snapshotFile, byte[] payload) {
        this.snapshotFile = snapshotFile;
        this.payload = payload;
    }

    public SnapshotFile getSnapshotFile() {
        return snapshotFile;
    }

    public byte[] getPayload() {
        return payload;
    }
}
