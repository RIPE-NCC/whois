package net.ripe.db.nrtm4;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SnapshotSynchronizer {

    void synchronizeDeltasToSnapshot(final List<DeltaChange> changes) {
        // TODO: apply changes to the snapshot
        // if 'operation' is '1' (add) then add to snapshot table
        // if 'operation' is '2' (del) then remove from snapshot table

        // TODO: throw exception (NrtmProcessException?) if operation can't be done coz it
        //       indicates we've gone out of sync -- re-initialize the source and publish snapshot 1
    }

}
