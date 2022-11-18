package net.ripe.db.nrtm4;

public class SnapshotObjectCreator {

    public void initializeSnapshot() {

        // Find whois objects which are in the 'last' table

        // Add them to the snapshot table

    }

    public void validateSnapshot() {

        // Find whois objects which are in the 'last' table

        // Serialize them and compare with what's in the snapshot table
    }

    public void synchronizeObjects(final int serialId) {

        // Find serials since the one given

        // if 'operation' is '1' (add) then add to snapshot table and create a delta

        // if 'operation' is '2' (del) then remove from snapshot table and create a delta

    }

}
