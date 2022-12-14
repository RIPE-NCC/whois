package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import org.springframework.stereotype.Service;


@Service
public class SnapshotSynchronizer {

    private final SnapshotObjectRepository snapshotObjectRepository;

    SnapshotSynchronizer(
        final SnapshotObjectRepository snapshotObjectRepository
    ) {
        this.snapshotObjectRepository = snapshotObjectRepository;
    }

    void synchronizeDeltasToSnapshot(final PublishableDeltaFile deltaFile) {
        for (final DeltaChange change : deltaFile.getChanges()) {
            if (change.getAction() == DeltaChange.Action.ADD_MODIFY) {
                snapshotObjectRepository.insert(
                    deltaFile.getVersionId(),
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
