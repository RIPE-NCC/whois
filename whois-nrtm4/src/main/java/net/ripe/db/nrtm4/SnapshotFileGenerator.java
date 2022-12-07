package net.ripe.db.nrtm4;

import com.google.common.hash.Hashing;
import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.PublishedFileRepository;
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
    private final PublishedFileRepository publishedFileRepository;

    public SnapshotFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotInitializer snapshotInitializer,
        final SnapshotFileStreamer snapshotFileStreamer,
        final PublishedFileRepository publishedFileRepository
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotInitializer = snapshotInitializer;
        this.snapshotFileStreamer = snapshotFileStreamer;
        this.publishedFileRepository = publishedFileRepository;
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
        final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(version);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        try {
            snapshotFileStreamer.processSnapshot(snapshotFile, bos);
            System.out.println(bos.toString(StandardCharsets.UTF_8));
            // todo: calculate random for url
            final String sha256hex = Hashing.sha256()
                .hashString(bos.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                .toString();
            final String fileName = String.format("nrtm-snapshot.%d.xxxxxxxxxx", version.getVersion());
            publishedFileRepository.save(
                snapshotFile.getVersionId(),
                NrtmDocumentType.SNAPSHOT,
                fileName,
                sha256hex);
            snapshotFile.setFileName(fileName);
            snapshotFile.setHash(sha256hex);
            return Optional.of(snapshotFile);
        } catch (final IOException e) {
            LOGGER.error("Exception thrown when generating snapshot file", e);
            throw new RuntimeException(e);
        }
    }

}
