package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class NotificationFileGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFileGenerationService.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotInitializer snapshotInitializer;

    public NotificationFileGenerationService(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotInitializer snapshotInitializer
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotInitializer = snapshotInitializer;
    }

    @Transactional
    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public PublishableSnapshotFile generateSnapshot(final NrtmSource source) {

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
            }
        }
        final PublishableSnapshotFile publishableSnapshotFile = new PublishableSnapshotFile(version);
        publishableSnapshotFile.setObjectsString("");
        return publishableSnapshotFile;
    }

}
