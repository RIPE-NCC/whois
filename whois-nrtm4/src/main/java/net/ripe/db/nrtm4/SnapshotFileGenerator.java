package net.ripe.db.nrtm4;

import com.google.common.hash.Hashing;
import net.ripe.db.nrtm4.persist.DeltaFileRepository;
import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.SnapshotFileRepository;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.nrtm4.publish.SnapshotFileStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotInitializer snapshotInitializer;
    private final SnapshotFileStreamer snapshotFileStreamer;
    private final DeltaFileRepository deltaFileRepository;
    private final SnapshotFileRepository snapshotFileRepository;

    public SnapshotFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotInitializer snapshotInitializer,
        final SnapshotFileStreamer snapshotFileStreamer,
        final DeltaFileRepository deltaFileRepository,
        final SnapshotFileRepository snapshotFileRepository
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotInitializer = snapshotInitializer;
        this.snapshotFileStreamer = snapshotFileStreamer;
        this.deltaFileRepository = deltaFileRepository;
        this.snapshotFileRepository = snapshotFileRepository;
    }

    @Transactional
    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public Optional<PublishableSnapshotFile> generateSnapshot(final NrtmSource source) {

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
                LOGGER.info("Not generating snapshot file since there have been no changes since v{} with serialID {}",
                    version.getVersion(), version.getLastSerialId());
                return Optional.empty();
            }
        }
        // TODO: apply pending deltas
        //       * if snapshot version > 1...
        //           - find list of deltas since the last snapshot
        //           - process them with SnapshotSynchronizer to bring snapshot_objects up to date
        final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(version);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        try {
            final String fileName = FileNameGenerator.snapshotFileName(version.getVersion());
            //final OutputStream out = nrtmFileRepo.getFileOutputStream(fileName);
            snapshotFileStreamer.processSnapshot(snapshotFile, bos);
            final String payload = bos.toString(StandardCharsets.UTF_8);
            final String sha256hex = Hashing.sha256()
                .hashString(payload, StandardCharsets.UTF_8)
                .toString();
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

}
