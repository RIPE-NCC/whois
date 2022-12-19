package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.SnapshotObject;
import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class SnapshotSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotSynchronizer.class);

    private final DeltaTransformer deltaTransformer;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;

    SnapshotSynchronizer(
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.snapshotObjectRepository = snapshotObjectRepository;
    }

    boolean synchronizeDeltasToSnapshot(final NrtmSource source, final NrtmVersionInfo version) {
        final NrtmVersionInfo lastSnapshot = nrtmVersionInfoRepository.findLastSnapshotVersion(source);
        final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesBetween(lastSnapshot.getLastSerialId(), version.getLastSerialId());
        final List<DeltaChange> deltas = deltaTransformer.toDeltaChange(whoisChanges);
        if (deltas.size() < 1) {
            return false;
        }

        for (final DeltaChange change : deltas) {
            if (change.getAction() == DeltaChange.Action.ADD_MODIFY) {
                final Optional<SnapshotObject> existing = snapshotObjectRepository.getByObjectTypeAndPrimaryKey(change.getObjectType(), change.getPrimaryKey());
                if (existing.isPresent()) {
                    snapshotObjectRepository.update(
                        version.getId(),
                        change.getSerialId(),
                        change.getObject().getKey().toString(),
                        change.getObject().toString()
                    );
                } else {
                    snapshotObjectRepository.insert(
                        version.getId(),
                        change.getSerialId(),
                        change.getObject().getType(),
                        change.getObject().getKey().toString(),
                        change.getObject().toString()
                    );
                }
            } else if (change.getAction() == DeltaChange.Action.DELETE) {
                snapshotObjectRepository.delete(change.getObjectType(), change.getPrimaryKey());
            }
        }
        return true;
    }

}
