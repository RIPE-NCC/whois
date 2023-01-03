package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotObject;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SnapshotObjectSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotObjectSynchronizer.class);

    private final DeltaTransformer deltaTransformer;
    private final Dummifier dummifierNrtm;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;

    SnapshotObjectSynchronizer(
        final DeltaTransformer deltaTransformer,
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository
    ) {
        this.deltaTransformer = deltaTransformer;
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.snapshotObjectRepository = snapshotObjectRepository;
    }

    NrtmVersionInfo init(final NrtmSource source) {
        LOGGER.info("getSerialEntriesFromLast() entered");
        final SerialRange serialRange = serialDao.getSerials();
        final int lastSerial = serialRange.getEnd();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialSnapshot(source, lastSerial);
        serialDao.getSerialEntriesFromLast(rs -> {
            final SerialEntry serialEntry = new SerialEntry(
                rs.getInt(1),
                Operation.getByCode(rs.getInt(2)),
                rs.getBoolean(3),
                rs.getInt(4),
                rs.getBytes(5),
                rs.getString(6));
            if (dummifierNrtm.isAllowed(NrtmConstants.NRTM_VERSION, serialEntry.getRpslObject())) {
                snapshotObjectRepository.insert(
                    version.getId(),
                    serialEntry.getSerialId(),
                    serialEntry.getRpslObject().getType(),
                    serialEntry.getPrimaryKey(),
                    dummifierNrtm.dummify(NrtmConstants.NRTM_VERSION, serialEntry.getRpslObject()).toString());
            }
        });
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
