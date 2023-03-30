package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmSource;
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
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    public NrtmFileProcessor(
        final DeltaFileGenerator deltaFileGenerator,
        final SnapshotFileGenerator snapshotFileGenerator,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.deltaFileGenerator = deltaFileGenerator;
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public void updateNrtmFilesAndPublishNotification() {
        LOGGER.info("updateNrtmFilesAndPublishNotification() called");
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final SnapshotState state = whoisObjectRepository.getSnapshotState();
        LOGGER.info("Found {} objects in {}", state.whoisObjectData().size(), stopwatch);
        final List<NrtmSource> sourceList = sourceRepository.getSources();
        if (sourceList.isEmpty()) {
            LOGGER.info("Initializing...");
            snapshotFileGenerator.createInitialSnapshots(state);
            LOGGER.info("Initialization complete. Total time: {}", stopwatch);
        } else {
            // Must do deltas first since snapshot creation is skipped if there aren't any
            deltaFileGenerator.createDeltas(state.serialId());
            snapshotFileGenerator.createSnapshots(state);
        }
    }

}
