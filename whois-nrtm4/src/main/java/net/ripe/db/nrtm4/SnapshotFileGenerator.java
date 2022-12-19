package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.SnapshotFile;
import net.ripe.db.nrtm4.persist.SnapshotFileRepository;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.nrtm4.publish.SnapshotFileStreamer;
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

    private final DeltaTransformer deltaTransformer;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotInitializer snapshotInitializer;
    private final SnapshotFileStreamer snapshotFileStreamer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotSynchronizer snapshotSynchronizer;
    private final NrtmFileStore nrtmFileStore;
    private final NrtmFileUtil nrtmFileUtil;

    public SnapshotFileGenerator(
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotInitializer snapshotInitializer,
        final SnapshotFileStreamer snapshotFileStreamer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotSynchronizer snapshotSynchronizer,
        final NrtmFileStore nrtmFileStore,
        final NrtmFileUtil nrtmFileUtil
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotInitializer = snapshotInitializer;
        this.snapshotFileStreamer = snapshotFileStreamer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotSynchronizer = snapshotSynchronizer;
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmFileUtil = nrtmFileUtil;
    }

    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public Optional<PublishableSnapshotFile> createSnapshot(final NrtmSource source) {

        // Get last version from database.
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        NrtmVersionInfo version;
        if (lastVersion.isEmpty()) {
            version = snapshotInitializer.init(source);
        } else {
            version = lastVersion.get();
            if (version.getType() == NrtmDocumentType.DELTA) {
                version = nrtmVersionInfoRepository.copyAsSnapshotVersion(version);
            } else {
                LOGGER.info("Not generating snapshot file since no deltas have been published since v{} with serialID {}",
                    version.getVersion(), version.getLastSerialId());
                return Optional.empty();
            }
        }
        // TODO: apply pending deltas
        //       * if snapshot version > 1...
        //           - find list of deltas since the last snapshot
        //           - process them with SnapshotSynchronizer to bring snapshot_objects up to date
        if (version.getVersion() > 1) {
            final boolean snapshotWasUpdated = snapshotSynchronizer.synchronizeDeltasToSnapshot(source, version);
            if (!snapshotWasUpdated) {
                return Optional.empty();
            }
        }
        final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(version);
        //final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        try {
            final String fileName = nrtmFileUtil.fileName(snapshotFile);
            final OutputStream out = nrtmFileStore.getFileOutputStream(fileName);
            snapshotFileStreamer.writeSnapshotAsJson(snapshotFile, out);
            out.close();
            final String sha256hex = DigestUtils.sha256Hex(nrtmFileStore.getFileInputStream(fileName));
            snapshotFileRepository.save(
                snapshotFile.getVersionId(),
                fileName,
                sha256hex
            );
            snapshotFile.setFileName(fileName);
            snapshotFile.setHash(sha256hex);
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