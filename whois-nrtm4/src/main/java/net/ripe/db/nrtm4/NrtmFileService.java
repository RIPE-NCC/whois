package net.ripe.db.nrtm4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


@Service
public class NrtmFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileService.class);

    public static final int MAX_LENGTH_SESSION_ID = 256;
    public static final int MAX_LENGTH_FILE_NAME = 256;
    private final NrtmFileStore nrtmFileStore;
    private final NrtmFileSync nrtmFileSync;

    NrtmFileService(
        final NrtmFileStore nrtmFileStore,
        final NrtmFileSync nrtmFileSync
    ) {
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmFileSync = nrtmFileSync;
    }

    void writeFileToStream(final String sessionId, final String name, final OutputStream out) throws IOException {
        if (sessionId == null || name == null || sessionId.length() > MAX_LENGTH_SESSION_ID || name.length() > MAX_LENGTH_FILE_NAME) {
            throw new IllegalArgumentException("Invalid NRTM sessionID / file name");
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
            //nrtmFileSync.syncDeltaFromDbToDisk(sessionId, name);
            LOGGER.debug("skipping deltas");
        } else if (name.startsWith(SNAPSHOT_PREFIX)) {
            nrtmFileSync.syncSnapshotFromDbToDisk(sessionId, name);
        }
    }

}
