package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.VersionInformation;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class VersionControl {

    void createNewSource(final NrtmSource source) {
        // Delete rows referring to old source
        // Create a new row for source
        // Generate a session ID
        // Set version to 0 where zero means, "no published snapshot yet"
        // - Assumes a new snapshot will be published in the same transaction (so there will never actually be a stored
        //   row where version is 0
    }

    Optional<VersionInformation> getLastPublishedVersion(final NrtmSource source) {
        // Find the highest version for this source. return empty if not found.
        return Optional.empty();
    }

    public VersionInformation publishVersion(final VersionInformation versionInformation) {
        return versionInformation.incrementVersion();
    }

}
