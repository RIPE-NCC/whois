package net.ripe.db.nrtm4;

import com.google.common.util.concurrent.Monitor;
import net.ripe.db.nrtm4.persist.DeltaFile;
import net.ripe.db.nrtm4.persist.DeltaFileRepository;
import net.ripe.db.nrtm4.persist.NotificationFile;
import net.ripe.db.nrtm4.persist.NotificationFileRepository;
import net.ripe.db.nrtm4.persist.SnapshotFile;
import net.ripe.db.nrtm4.persist.SnapshotFileRepository;
import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.NOTIFICATION_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


public class NrtmFileService {

    private final NotificationFileRepository notificationFileRepository;
    private final NrtmFileRepo nrtmFileRepo;
    private final DeltaFileRepository deltaFileRepository;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final Monitor deltaMutex = new Monitor();
    private final Monitor snapshotMutex = new Monitor();

    NrtmFileService(
        final NotificationFileRepository notificationFileRepository,
        final NrtmFileRepo nrtmFileRepo,
        final DeltaFileRepository deltaFileRepository,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotObjectRepository snapshotObjectRepository
    ) {
        this.notificationFileRepository = notificationFileRepository;
        this.nrtmFileRepo = nrtmFileRepo;
        this.deltaFileRepository = deltaFileRepository;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotObjectRepository = snapshotObjectRepository;
    }

    void getFile(final String name, final OutputStream out) throws IOException {
        if (name == null || name.length() > 1024) {
            throw new IllegalArgumentException("Invalid NRTM file name");
        }
        if (name.startsWith(NOTIFICATION_PREFIX)) {
            final NotificationFile notificationFile = notificationFileRepository.getNotificationFile();
            out.write(notificationFile.getPayload().getBytes(StandardCharsets.UTF_8));
        } else if (name.startsWith(DELTA_PREFIX)) {
            if (!nrtmFileRepo.checkIfFileExists(name)) {
                deltaMutex.enter();
                try {
                    // check again now that the lock is in force
                    if (!nrtmFileRepo.checkIfFileExists(name)) {
                        final Optional<DeltaFile> optDeltaFile = deltaFileRepository.getByName(name);
                        if (optDeltaFile.isPresent()) {
                            nrtmFileRepo.storeFile(name, optDeltaFile.get().getPayload());
                        } else {
                            throw new FileNotFoundException("NRTM has no files with name: " + name);
                        }
                    }
                } finally {
                    deltaMutex.leave();
                }
            }
            nrtmFileRepo.streamFromFile(name, out);
        } else if (name.startsWith(SNAPSHOT_PREFIX)) {
            if (!nrtmFileRepo.checkIfFileExists(name)) {
                snapshotMutex.enter();
                try {
                    // check again now that the lock is in force
                    if (!nrtmFileRepo.checkIfFileExists(name)) {
                        final Optional<SnapshotFile> snapshotFile = snapshotFileRepository.getByName(name);
                        if (snapshotFile.isPresent()) {
                            final FileOutputStream fos = nrtmFileRepo.getFileOutputStream(name);
                            snapshotObjectRepository.streamSnapshot(fos);
                        } else {
                            throw new FileNotFoundException("NRTM has no files with name: " + name);
                        }
                    }
                } finally {
                    snapshotMutex.leave();
                }
            }
            nrtmFileRepo.streamFromFile(name, out);
        } else {
            throw new IllegalArgumentException("Not an NRTM file name: " + name);
        }
    }

}
