package net.ripe.db.nrtm4;

import com.google.common.util.concurrent.Monitor;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;


@Service
public class NrtmFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileService.class);

    private static final int BUFFER_SIZE = 1024 * 1024;
    public static final int MAX_LENGTH_SESSION_ID = 256;
    public static final int MAX_LENGTH_FILE_NAME = 256;

    private final Monitor snapshotMutex = new Monitor();
    private final String path;
    private final SnapshotFileRepository snapshotFileRepository;

    NrtmFileService(
        @Value("${nrtm.file.path}") final String path,
        final SnapshotFileRepository snapshotFileRepository
    ) {
        this.path = path;
        this.snapshotFileRepository = snapshotFileRepository;
    }

    public void createNrtmSessionDirectory(final String sessionId) {
        final File dir = new File(path, sessionId);
        if (dir.exists()) {
            throw new RuntimeException("Failed to create NRTM directory because it already exists" + path + " " + sessionId);
        }
        if (!dir.mkdir()) {
            LOGGER.error("Could not create directory for sessionID: " + sessionId);
            throw new RuntimeException("Failed to create NRTM directory " + path + " " + sessionId);
        }
    }

    public void writeToDisk(final PublishableNrtmFile file, final byte[] bytes) throws IOException {
        final File dir = new File(path, file.getSessionID());
        final OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(new File(dir, file.getFileName())), BUFFER_SIZE);
        fileOut.write(bytes);
        fileOut.flush();
    }

    void writeFileToStream(final String sessionId, final String name, final OutputStream out) throws IOException {
        if (sessionId == null || name == null || sessionId.length() > MAX_LENGTH_SESSION_ID || name.length() > MAX_LENGTH_FILE_NAME) {
            throw new IllegalArgumentException("Invalid NRTM sessionID / file name");
        }
        syncNrtmFileToFileSystem(sessionId, name);
        try (final FileInputStream fis = NrtmFileUtil.getFileInputStream(path, sessionId, name)) {
            fis.transferTo(out);
        }
    }

    void syncNrtmFileToFileSystem(final String sessionId, final String name) throws IOException {
        if (!name.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix()) &&
            !name.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            throw new IllegalArgumentException("Not an NRTM file name: " + name);
        }
        if (NrtmFileUtil.checkIfFileExists(path, sessionId, name)) {
            return;
        }
        if (name.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            LOGGER.debug("skipping deltas");
        } else if (name.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            snapshotMutex.enter();
            try {
                // check again now that the lock is in force
                if (NrtmFileUtil.checkIfFileExists(path, sessionId, name)) {
                    return;
                }
                final Optional<SnapshotFile> snapshotFile = snapshotFileRepository.getByName(sessionId, name);
                // should always be the last version, since we only maintain the latest snapshot
                if (snapshotFile.isEmpty()) {
                    throw new FileNotFoundException("NRTM has no snapshot files with name: " + name);
                }
//            final NrtmVersionInfo version = nrtmVersionInfoRepository.findById(snapshotFile.get().getVersionId());
//            final PublishableSnapshotFile publishableSnapshotFile = new PublishableSnapshotFile(version);
//            try (final OutputStream out = nrtmFileStore.getFileOutputStream(sessionId, name)) {
//                snapshotFileSerializer.writeSnapshotAsJson(publishableSnapshotFile, out);
//            }
            } finally {
                snapshotMutex.leave();
            }
        }
    }
}
