package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFile;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFileStreamer;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Optional;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileStreamer snapshotFileStreamer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotObjectSynchronizer snapshotObjectSynchronizer;
    private final NrtmFileStore nrtmFileStore;

    public SnapshotFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotFileStreamer snapshotFileStreamer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotObjectSynchronizer snapshotObjectSynchronizer,
        final NrtmFileStore nrtmFileStore
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileStreamer = snapshotFileStreamer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotObjectSynchronizer = snapshotObjectSynchronizer;
        this.nrtmFileStore = nrtmFileStore;
    }

    public Optional<PublishableSnapshotFile> createSnapshot(final NrtmSource source) {
        final String method = "createSnapshot";
        // Get last version from database.
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        NrtmVersionInfo version;
        LOGGER.info("{} lastVersion.isEmpty() {}", method, lastVersion.isEmpty());
        if (lastVersion.isEmpty()) {
            version = snapshotObjectSynchronizer.initializeSnapshotObjects(source);
        } else {
            version = lastVersion.get();
            if (version.getType() == NrtmDocumentType.DELTA) {
                version = nrtmVersionInfoRepository.copyAsSnapshotVersion(version);
            } else {
                LOGGER.info("{} Not generating snapshot file since no deltas have been published since v{} with serialID {}",
                    method, version.getVersion(), version.getLastSerialId());
                return Optional.empty();
            }
        }
        LOGGER.info("{} now at version: {}", method, version);
        if (version.getVersion() > 1) {
            LOGGER.debug("not syncing deltas to snapshot");
//            final boolean snapshotWasUpdated = snapshotObjectSynchronizer.synchronizeDeltasToSnapshot(source, version);
//            if (!snapshotWasUpdated) {
//                LOGGER.warn("{} Code execution should not reach this point since we've already tested for no deltas. " +
//                        "Version {}, last serial: {}",
//                    method, version.getVersion(), version.getLastSerialId());
//                return Optional.empty();
//            }
        }
        final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(version);
        final String fileName = NrtmFileUtil.newFileName(snapshotFile);
        try {
            final long start = System.currentTimeMillis();
            final OutputStream out = nrtmFileStore.getFileOutputStream(snapshotFile.getSessionID(), fileName);
            snapshotFileStreamer.writeSnapshotAsJson(snapshotFile, out);
            out.close();
            final String sha256hex = DigestUtils.sha256Hex(nrtmFileStore.getFileInputStream(snapshotFile.getSessionID(), fileName));
            snapshotFileRepository.save(
                snapshotFile.getVersionId(),
                fileName,
                sha256hex
            );
            snapshotFile.setFileName(fileName);
            snapshotFile.setHash(sha256hex);
            final long mark = System.currentTimeMillis();
            final DecimalFormat df = new DecimalFormat("#,###.000");
            LOGGER.info("{} Generated snapshot file in {} min", method, df.format((mark - start) / 60000.0));
            return Optional.of(snapshotFile);
        } catch (final IOException e) {
            LOGGER.error("Exception thrown when generating snapshot file", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSource source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}