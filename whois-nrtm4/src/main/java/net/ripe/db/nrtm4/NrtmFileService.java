package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NotificationFile;
import net.ripe.db.nrtm4.dao.NotificationFileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.NOTIFICATION_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


@Service
public class NrtmFileService {

    private final NotificationFileRepository notificationFileRepository;
    private final NrtmFileStore nrtmFileStore;
    private final NrtmFileSync nrtmFileSync;

    NrtmFileService(
        final NotificationFileRepository notificationFileRepository,
        final NrtmFileStore nrtmFileStore,
        final NrtmFileSync nrtmFileSync
    ) {
        this.notificationFileRepository = notificationFileRepository;
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmFileSync = nrtmFileSync;
    }

    void writeFileToStream(final String sessionId, final String name, final OutputStream out) throws IOException {
        if (sessionId == null || name == null || sessionId.length() > 256 || name.length() > 256) {
            throw new IllegalArgumentException("Invalid NRTM file name");
        }
        if (name.startsWith(NOTIFICATION_PREFIX)) {
            // TODO: how do we know which source to use when serving a notification request?
            //       Probably better to put it in url and create a separate method for writing notification.
            final NotificationFile notificationFile = notificationFileRepository.getNotificationFile(1);
            out.write(notificationFile.getPayload().getBytes(StandardCharsets.UTF_8));
            return;
        }
        syncNrtmFileToFileSystem(sessionId, name);
        nrtmFileStore.streamFromFile(sessionId, name, out);
    }

    void syncNrtmFileToFileSystem(final String sessionId, final String name) throws IOException {
        if (!name.startsWith(DELTA_PREFIX) && !name.startsWith(SNAPSHOT_PREFIX)) {
            throw new IllegalArgumentException("Not an NRTM file name: " + name);
        }
        if (nrtmFileStore.checkIfFileExists(sessionId, name)) {
            return;
        }
        if (name.startsWith(DELTA_PREFIX)) {
            nrtmFileSync.syncDeltaFromDbToDisk(sessionId, name);
        } else if (name.startsWith(SNAPSHOT_PREFIX)) {
            nrtmFileSync.syncSnapshotFromDbToDisk(sessionId, name);
        }
    }

}
