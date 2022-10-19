package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.VersionInformation;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class SnapshotPublisher {

    private final VersionControl versionControl;

    SnapshotPublisher(
            final VersionControl versionControl
    ) {
        this.versionControl = versionControl;
    }

    void publishSnapshot(final NrtmSource source) {
        final Optional<VersionInformation> version = versionControl.getLastPublishedVersion(source);
        if (version.isEmpty()) {
            versionControl.createNewSource(source);
            // TODO publish the snapshot
//        } else {
//            final VersionInformation nextVersion = versionControl.publishVersion(version.get());
        }
    }

}
