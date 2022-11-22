package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.SerialModel;
import net.ripe.db.nrtm4.persist.WhoisSlaveDao;

import java.util.List;


public class SnapshotObjectCreator {

    private DeltaProcessor deltaProcessor;
    private final WhoisSlaveDao whoisSlaveDao;

    public SnapshotObjectCreator(
            final DeltaProcessor deltaProcessor,
            final WhoisSlaveDao whoisSlaveDao
            ) {
        this.deltaProcessor = deltaProcessor;
        this.whoisSlaveDao = whoisSlaveDao;
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
        final int lastSerialId = deltaProcessor.findLastSerialId();
        final List<SerialModel> serials = whoisSlaveDao.findSerialsSince(lastSerialId);

        // Create a delta

        // if 'operation' is '1' (add) then add to snapshot table

        // if 'operation' is '2' (del) then remove from snapshot table

    }

}
