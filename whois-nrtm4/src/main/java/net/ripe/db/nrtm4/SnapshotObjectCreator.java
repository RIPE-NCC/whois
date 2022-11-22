package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.WhoisSlaveRepository;


public class SnapshotObjectCreator {

    private final WhoisSlaveRepository whoisSlaveRepository;

    public SnapshotObjectCreator(
            final WhoisSlaveRepository whoisSlaveRepository
    ) {
        this.whoisSlaveRepository = whoisSlaveRepository;
    }

    public void initializeSnapshot() {

        // Find whois objects which are in the 'last' table

        // Add them to the snapshot table

    }

    public void validateSnapshot() {

        // Find whois objects which are in the 'last' table

        // Serialize them and compare with what's in the snapshot table -- compare only primary keys?
        // ...or hash? if hash then it must only hash the object data, not the json data (since two
        // different json structures might actually contain the same objects)
    }

    public void synchronizeObjects() {

        // Find serials since the one given

        // Create a delta

        // if 'operation' is '1' (add) then add to snapshot table

        // if 'operation' is '2' (del) then remove from snapshot table

    }

}
