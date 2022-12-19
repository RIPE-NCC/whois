package net.ripe.db.nrtm4;

import com.google.common.util.concurrent.Monitor;
import net.ripe.db.nrtm4.dao.DeltaFile;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NotificationFile;
import net.ripe.db.nrtm4.dao.NotificationFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFileStreamer;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.NOTIFICATION_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


@Service
public class NrtmFileService {

    private final NotificationFileRepository notificationFileRepository;
    private final NrtmFileStore nrtmFileStore;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final DeltaFileRepository deltaFileRepository;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotFileStreamer snapshotFileStreamer;
    private final Monitor deltaMutex = new Monitor();
    private final Monitor snapshotMutex = new Monitor();

    NrtmFileService(
        final NotificationFileRepository notificationFileRepository,
        final NrtmFileStore nrtmFileStore,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final DeltaFileRepository deltaFileRepository,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileStreamer snapshotFileStreamer
    ) {
        this.notificationFileRepository = notificationFileRepository;
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.deltaFileRepository = deltaFileRepository;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotFileStreamer = snapshotFileStreamer;
    }

    void writeFileToStream(final String name, final OutputStream out) throws IOException {
        if (name == null || name.length() > 256) {
            throw new IllegalArgumentException("Invalid NRTM file name");
        }
        if (name.startsWith(NOTIFICATION_PREFIX)) {
            // TODO: how do we know which source to use when serving a notification request?
            final NotificationFile notificationFile = notificationFileRepository.getNotificationFile(1);
            out.write(notificationFile.getPayload().getBytes(StandardCharsets.UTF_8));
            return;
        }
        syncNrtmFileToFileSystem(name);
        nrtmFileStore.streamFromFile(name, out);
    }

    void syncNrtmFileToFileSystem(final String name) throws IOException {
        if (name.startsWith(DELTA_PREFIX)) {
            if (!nrtmFileStore.checkIfFileExists(name)) {
                deltaMutex.enter();
                try {
                    // check again now that the lock is in force
                    if (!nrtmFileStore.checkIfFileExists(name)) {
                        final Optional<DeltaFile> optDeltaFile = deltaFileRepository.getByName(name);
                        if (optDeltaFile.isPresent()) {
                            nrtmFileStore.storeFile(name, optDeltaFile.get().getPayload());
                        } else {
                            throw new FileNotFoundException("NRTM has no delta files with name: " + name);
                        }
                    }
                } finally {
                    deltaMutex.leave();
                }
            }
        } else if (name.startsWith(SNAPSHOT_PREFIX)) {
            if (!nrtmFileStore.checkIfFileExists(name)) {
                snapshotMutex.enter();
                try {
                    // check again now that the lock is in force
                    if (!nrtmFileStore.checkIfFileExists(name)) {
                        final Optional<SnapshotFile> snapshotFile = snapshotFileRepository.getByName(name);
                        // should always be the last version, since we only maintain the latest snapshot
                        if (snapshotFile.isPresent()) {
                            final NrtmVersionInfo version = nrtmVersionInfoRepository.findById(snapshotFile.get().getVersionId()).orElseThrow();
                            final PublishableSnapshotFile publishableSnapshotFile = new PublishableSnapshotFile(version);
                            final FileOutputStream fos = nrtmFileStore.getFileOutputStream(name);
                            snapshotFileStreamer.writeSnapshotAsJson(publishableSnapshotFile, fos);
                            fos.close();
                        } else {
                            throw new FileNotFoundException("NRTM has no snapshot files with name: " + name);
                        }
                    }
                } finally {
                    snapshotMutex.leave();
                }
            }
        } else {
            throw new IllegalArgumentException("Not an NRTM file name: " + name);
        }
    }

}
