package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileSerializer;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotObjectSynchronizer snapshotObjectSynchronizer;
    private final NrtmFileStore nrtmFileStore;

    public SnapshotFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotObjectSynchronizer snapshotObjectSynchronizer,
        final NrtmFileStore nrtmFileStore
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotObjectSynchronizer = snapshotObjectSynchronizer;
        this.nrtmFileStore = nrtmFileStore;
    }

    public Optional<PublishableSnapshotFile> createSnapshot(final NrtmSource source) throws IOException {
        // Get last version from database.
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        NrtmVersionInfo version;
        LOGGER.info("lastVersion.isEmpty() {}", lastVersion.isEmpty());
        if (lastVersion.isEmpty()) {
            version = snapshotObjectSynchronizer.initializeSnapshotObjects(source);
            nrtmFileStore.createNrtmSessionDirectory(version.getSessionID());
        } else {
            version = lastVersion.get();
            if (version.getType() == NrtmDocumentType.DELTA) {
                LOGGER.info("Not generating snapshot file yet");
                return Optional.empty();
            } else {
                LOGGER.info("Not generating snapshot file since no deltas have been published since v{} with serialID {}",
                    version.getVersion(), version.getLastSerialId());
                return Optional.empty();
            }
        }
        LOGGER.info("now at version: {}", version);
        if (version.getVersion() > 1) {
            LOGGER.debug("not syncing deltas to snapshot");
            return Optional.empty();
        }
        final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(version);
        final String fileName = NrtmFileUtil.newFileName(snapshotFile);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        try (final OutputStream out = nrtmFileStore.getFileOutputStream(snapshotFile.getSessionID(), fileName)) {
            snapshotFileSerializer.writeSnapshotAsJson(snapshotFile, out);
        }
        try {
            final String sha256hex = DigestUtils.sha256Hex(nrtmFileStore.getFileInputStream(snapshotFile.getSessionID(), fileName));
            snapshotFileRepository.save(
                snapshotFile.getVersionId(),
                fileName,
                sha256hex
            );
            snapshotFile.setFileName(fileName);
            snapshotFile.setHash(sha256hex);
            LOGGER.info("Generated snapshot file in {}", stopwatch);
            return Optional.of(snapshotFile);
        } catch (final IOException e) {
            LOGGER.error("Exception thrown when calculating hash of snapshot file", e);
            throw e;
        }
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSource source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}
