package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInformationDao;
import net.ripe.db.nrtm4.persist.VersionInformation;
import net.ripe.db.nrtm4.publish.SnapshotFile;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class NotificationFileGenerationService {

    private final NrtmVersionInformationDao versionDao;

    public NotificationFileGenerationService(
            final NrtmVersionInformationDao nrtmVersionInformationDao
    ) {
        versionDao = nrtmVersionInformationDao;
    }

    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public SnapshotFile generateSnapshot(final NrtmSource source) {

        // Get last version from database.
        final Optional<VersionInformation> lastVersion = versionDao.findLastVersion(source);
        VersionInformation version;
        if (lastVersion.isEmpty()) {
            version = versionDao.createNew(source);
        } else {
            version = lastVersion.get();
            // If it's a delta then use this version. If it's a snapshot then increment it.
            if (version.getType() == NrtmDocumentType.snapshot) {
                version = versionDao.incrementAndSave(version);
            }
        }
        final SnapshotFile snapshotFile = new SnapshotFile(version);
        return snapshotFile.setObjectsString("");
    }

}
