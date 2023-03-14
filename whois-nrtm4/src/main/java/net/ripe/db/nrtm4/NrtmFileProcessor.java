package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.SnapshotState;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NrtmFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileProcessor.class);

    private final DeltaFileGenerator deltaFileGenerator;
    private final NotificationFileGenerator notificationFileGenerator;
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    public NrtmFileProcessor(
        final DeltaFileGenerator deltaFileGenerator,
        final NotificationFileGenerator notificationFileGenerator,
        final SnapshotFileGenerator snapshotFileGenerator,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.deltaFileGenerator = deltaFileGenerator;
        this.notificationFileGenerator = notificationFileGenerator;
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public void updateNrtmFilesAndPublishNotification() {
        LOGGER.info("updateNrtmFilesAndPublishNotification() called");
        final SnapshotState state = whoisObjectRepository.getSnapshotState();
        final List<NrtmSourceModel> sourceList = sourceRepository.getAllSources();
        if (sourceList.isEmpty()) {
            sourceRepository.createSources();
            LOGGER.info("Initializing...");
            final List<NrtmVersionInfo> versions = snapshotFileGenerator.createSnapshots(state);
            versions.forEach(notificationFileGenerator::createInitialNotification);
            LOGGER.info("Initialization complete");
        } else {
            // Must do deltas first since snapshot creation is skipped if there aren't any
            deltaFileGenerator.createDeltas(state.serialId());
            snapshotFileGenerator.createSnapshots(state);
            notificationFileGenerator.updateNotification();
        }
        // TODO: optionally create notification file in db...
        //   Get the last notification to see if anything changed now that we might have generated more files
        //   If no snapshot was created for a source, keep the one from the last notification
        //   Get deltas which are < 24 hours old
        //   Don't publish a new one if the files are the same and the last notification is less than a day old
    }

}
