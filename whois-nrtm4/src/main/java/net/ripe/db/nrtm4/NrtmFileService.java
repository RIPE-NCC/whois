package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


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
        if (!name.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix()) &&
                !name.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            throw new IllegalArgumentException("Not an NRTM file name: " + name);
        }
        if (nrtmFileStore.checkIfFileExists(sessionId, name)) {
            return;
        }
        if (name.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            //nrtmFileSync.syncDeltaFromDbToDisk(sessionId, name);
            LOGGER.debug("skipping deltas");
        } else if (name.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            nrtmFileSync.syncSnapshotFromDbToDisk(sessionId, name);
        }
    }

}
