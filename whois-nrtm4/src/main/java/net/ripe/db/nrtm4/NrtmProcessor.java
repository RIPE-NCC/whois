package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFileModel;
import net.ripe.db.nrtm4.persist.DeltaFileModelRepository;
import net.ripe.db.nrtm4.persist.RpslObjectModel;
import net.ripe.db.nrtm4.persist.SerialModel;
import net.ripe.db.nrtm4.persist.WhoisSlaveDao;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NrtmProcessor {

    private final DeltaFileModelRepository deltaFileModelRepository;
    private final DeltaProcessor deltaProcessor;
    private final WhoisSlaveDao whoisSlaveDao;

    public NrtmProcessor(
            final WhoisSlaveDao whoisSlaveDao,
            final DeltaFileModelRepository deltaFileModelRepository,
            final DeltaProcessor deltaProcessor
    ) {
        this.whoisSlaveDao = whoisSlaveDao;
        this.deltaFileModelRepository = deltaFileModelRepository;
        this.deltaProcessor = deltaProcessor;
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

    public void processDeltas() {

        // Find changes since the last delta
        final DeltaFileModel deltaFileModel = deltaFileModelRepository.findLastChange();
        final int lastSerialId = deltaFileModel.getLastSerialId();
        final List<Pair<SerialModel, RpslObjectModel>> changes = whoisSlaveDao.findSerialsAndObjectsSinceSerial(lastSerialId);

        // TODO: Create a delta file
        final List<DeltaChange> deltaChangeList = deltaProcessor.process(changes);

        // TODO: apply changes to the snapshot
        // if 'operation' is '1' (add) then add to snapshot table
        // if 'operation' is '2' (del) then remove from snapshot table

    }

}
