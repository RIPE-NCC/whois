package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.VersionInformation;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class NotificationFileGenerationService {

    private final NrtmVersionInfoRepository versionDao;

    public NotificationFileGenerationService(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository
    ) {
        versionDao = nrtmVersionInfoRepository;
    }

    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public PublishableSnapshotFile generateSnapshot(final NrtmSource source) {

        // Get last version from database.
        final Optional<VersionInformation> lastVersion = versionDao.findLastVersion(source);
        VersionInformation version;
        if (lastVersion.isEmpty()) {
            version = versionDao.createInitialSnapshot(source, 0);
        } else {
            version = lastVersion.get();
            // TODO: don't increment -- just skip it -- no new snapshots if has it not changed
            //       since the last snapshot (see RFC)
            if (version.getType() == NrtmDocumentType.delta) {
                version = versionDao.copyAsSnapshotVersion(version);
            }
        }
        final PublishableSnapshotFile publishableSnapshotFile = new PublishableSnapshotFile(version);
        publishableSnapshotFile.setObjectsString("");
        return publishableSnapshotFile;
    }

}
