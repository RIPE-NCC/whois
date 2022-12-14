package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SnapshotSynchronizer {

    private final SnapshotObjectRepository snapshotObjectRepository;
    private final DeltaTransformer deltaTransformer;

    SnapshotSynchronizer(
        final SnapshotObjectRepository snapshotObjectRepository,
        final DeltaTransformer deltaTransformer
    ) {
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.deltaTransformer = deltaTransformer;
    }

    void synchronizeDeltasToSnapshot(final List<SerialEntry> whoisChanges, final long versionId) {
        final List<DeltaChange> deltas = deltaTransformer.toDeltaChange(whoisChanges);
        for (final DeltaChange change : deltas) {
            if (change.getAction() == DeltaChange.Action.ADD_MODIFY) {
                snapshotObjectRepository.insert(
                    versionId,
                    change.getSerialId(),
                    change.getObject().getType(),
                    change.getObject().getKey().toString(),
                    change.getObject().toString()
                );
            } else if (change.getAction() == DeltaChange.Action.DELETE) {
                snapshotObjectRepository.delete(change.getObjectType(), change.getPrimaryKey());
            }
        }
    }

}
