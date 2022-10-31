package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionDao;
import net.ripe.db.nrtm4.persist.VersionInformation;
import net.ripe.db.nrtm4.publish.SnapshotFile;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class NotificationFileGenerationService {

    private final NrtmVersionDao versionDao;

    public NotificationFileGenerationService(
            final NrtmVersionDao nrtmVersionDao
    ) {
        versionDao = nrtmVersionDao;
    }

    // TODO: Add a global lock to ensure that no other instance can run until this method exits
    public SnapshotFile generateSnapshot(final NrtmSource source) {

        // Get last version from database. If it's a delta then use this version. If it's a snapshot then increment it.
        final Optional<VersionInformation> lastVersion = versionDao.findLastVersion(source);
        VersionInformation version;
        if (lastVersion.isEmpty()) {
            version = versionDao.createNew(source);
        } else {
            version = lastVersion.get();
        }
        final SnapshotFile snapshotFile = new SnapshotFile(version);
        // TODO: Get NRTM objects as raw JSON string
        snapshotFile.setObjectsString("");
        return snapshotFile;
    }

}
