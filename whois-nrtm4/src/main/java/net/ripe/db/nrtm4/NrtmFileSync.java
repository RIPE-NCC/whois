package net.ripe.db.nrtm4;

import com.google.common.util.concurrent.Monitor;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileSerializer;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;


@Service
public class NrtmFileSync {

    private final Monitor snapshotMutex = new Monitor();
    private final NrtmFileStore nrtmFileStore;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;

    NrtmFileSync(
        final NrtmFileStore nrtmFileStore,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer
    ) {
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
    }

    void syncSnapshotFromDbToDisk(final String sessionId, final String name) throws IOException {
        snapshotMutex.enter();
        try {
            // check again now that the lock is in force
            if (nrtmFileStore.checkIfFileExists(sessionId, name)) {
                return;
            }
            final Optional<SnapshotFile> snapshotFile = snapshotFileRepository.getByName(sessionId, name);
            // should always be the last version, since we only maintain the latest snapshot
            if (snapshotFile.isEmpty()) {
                throw new FileNotFoundException("NRTM has no snapshot files with name: " + name);
            }
            final NrtmVersionInfo version = nrtmVersionInfoRepository.findById(snapshotFile.get().getVersionId()).orElseThrow();
            final PublishableSnapshotFile publishableSnapshotFile = new PublishableSnapshotFile(version);
            try (final OutputStream out = nrtmFileStore.getFileOutputStream(sessionId, name)) {
                snapshotFileSerializer.writeSnapshotAsJson(publishableSnapshotFile, out);
            }
        } finally {
            snapshotMutex.leave();
        }
    }

}
