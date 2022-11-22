package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFileModel;
import net.ripe.db.nrtm4.persist.DeltaFileModelRepository;
import net.ripe.db.nrtm4.persist.RpslObjectModel;
import net.ripe.db.nrtm4.persist.SerialModel;
import net.ripe.db.nrtm4.persist.WhoisSlaveDao;
import org.javatuples.Pair;

import java.util.List;


public class SnapshotObjectCreator {

    private final DeltaFileModelRepository deltaFileModelRepository;
    private final WhoisSlaveDao whoisSlaveDao;

    public SnapshotObjectCreator(
            final WhoisSlaveDao whoisSlaveDao,
            final DeltaFileModelRepository deltaFileModelRepository
    ) {
        this.whoisSlaveDao = whoisSlaveDao;
        this.deltaFileModelRepository = deltaFileModelRepository;
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

        final DeltaFileModel deltaFileModel = deltaFileModelRepository.findLastChange();
        final int lastSerialId = deltaFileModel.getLastSerialId();
        final List<Pair<SerialModel, RpslObjectModel>> changes = whoisSlaveDao.findSerialsAndObjectsSinceSerial(lastSerialId);

        // Create a delta

        // if 'operation' is '1' (add) then add to snapshot table

        // if 'operation' is '2' (del) then remove from snapshot table

    }

}
