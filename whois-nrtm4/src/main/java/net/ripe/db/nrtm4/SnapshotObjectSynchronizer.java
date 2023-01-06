package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.InitialSnapshotState;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.ObjectData;
import net.ripe.db.nrtm4.dao.SnapshotObject;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.nrtm4.dao.WhoisDao;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;
import static net.ripe.db.nrtm4.util.ListUtil.makeBatches;


@Service
public class SnapshotObjectSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotObjectSynchronizer.class);
    private static final int INSERT_BATCH_SIZE = 50;

    private final DeltaTransformer deltaTransformer;
    private final Dummifier dummifierNrtm;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final WhoisDao whoisDao;

    SnapshotObjectSynchronizer(
        final DeltaTransformer deltaTransformer,
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        @Qualifier("whoisSlaveSerialDao") final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository,
        final WhoisDao whoisDao
    ) {
        this.deltaTransformer = deltaTransformer;
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.whoisDao = whoisDao;
    }

    NrtmVersionInfo initializeSnapshotObjects(final NrtmSource source) {
        final String method = "initializeSnapshotObjects";
        long mark = System.currentTimeMillis();
        LOGGER.info("{} entered", method);
        final InitialSnapshotState initialState = whoisDao.getInitialSnapshotState();
        LOGGER.info("{} Found {} objects", method, initialState.objectData().size());
        LOGGER.info("{} At serial {}, {}ms", method, initialState.serialId(), (System.currentTimeMillis() - mark));
        mark = System.currentTimeMillis();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, initialState.serialId());
        makeBatches(initialState.objectData(), INSERT_BATCH_SIZE)
            .parallelStream()
            .forEach((objectBatch) -> {
                    final List<SnapshotObject> batch = new ArrayList<>(INSERT_BATCH_SIZE);
                    for (final ObjectData object : objectBatch) {
                        final String rpsl = whoisDao.findRpsl(object.objectId(), object.sequenceId());
                        final RpslObject rpslObject = RpslObject.parse(rpsl);
                        if (!dummifierNrtm.isAllowed(NRTM_VERSION, rpslObject)) {
                            continue;
                        }
                        final RpslObject dummyRpsl = dummifierNrtm.dummify(NRTM_VERSION, rpslObject);
                        batch.add(new SnapshotObject(0, version.getId(), object.objectId(), object.sequenceId(), dummyRpsl.toString()));
                    }
                    snapshotObjectRepository.batchInsert(batch);
                }
            );
        LOGGER.info("{} Inserted snapshot objects {}ms", method, (System.currentTimeMillis() - mark));
        LOGGER.info("getSerialEntriesFromLast() completed");
        return version;
    }

    boolean synchronizeDeltasToSnapshot(final NrtmSource source, final NrtmVersionInfo version) {
        final NrtmVersionInfo lastSnapshot = nrtmVersionInfoRepository.findLastSnapshotVersion(source);
        final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesBetween(lastSnapshot.getLastSerialId(), version.getLastSerialId())
            .collect(Collectors.toList());
        final List<DeltaChange> deltas = deltaTransformer.toDeltaChange(whoisChanges);
        if (deltas.size() < 1) {
            return false;
        }

//        for (final DeltaChange change : deltas) {
//            if (change.getAction() == DeltaChange.Action.ADD_MODIFY) {
//                final Optional<SnapshotObject> existing = snapshotObjectRepository.getByObjectTypeAndPrimaryKey(change.getObjectType(), change.getPrimaryKey());
//                if (existing.isPresent()) {
//                    snapshotObjectRepository.update(
//                        version.getId(),
//                        change.getSerialId(),
//                        change.getObject().getKey().toString(),
//                        change.getObject().toString()
//                    );
//                } else {
//                    snapshotObjectRepository.insert(
//                        version.getId(),
//                        change.getSerialId(),
//                        change.getObject().getType(),
//                        change.getObject().getKey().toString(),
//                        change.getObject().toString()
//                    );
//                }
//            } else if (change.getAction() == DeltaChange.Action.DELETE) {
//                snapshotObjectRepository.delete(change.getObjectType(), change.getPrimaryKey());
//            }
//        }
        return true;
    }

}
