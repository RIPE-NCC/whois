package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.jmx.NrtmProcessControl;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class NrtmFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileProcessor.class);

    private final DeltaFileGenerator deltaFileGenerator;
    private final NrtmFileService nrtmFileService;
    private final NrtmProcessControl nrtmProcessControl;
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    public NrtmFileProcessor(
        final DeltaFileGenerator deltaFileGenerator,
        final NrtmFileService nrtmFileService,
        final NrtmProcessControl nrtmProcessControl,
        final SnapshotFileGenerator snapshotFileGenerator,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.deltaFileGenerator = deltaFileGenerator;
        this.nrtmFileService = nrtmFileService;
        this.nrtmProcessControl = nrtmProcessControl;
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public void updateNrtmFilesAndPublishNotification() {
        LOGGER.info("updateNrtmFilesAndPublishNotification() called");
        final SnapshotState state = whoisObjectRepository.getSnapshotState();
        final List<NrtmSourceModel> sourceList = sourceRepository.getAllSources();
        final Set<PublishableNrtmFile> snapshotFiles = new HashSet<>();
        final Set<PublishableDeltaFile> deltaFiles = new HashSet<>();
        if (sourceList.isEmpty()) {
            if (nrtmProcessControl.isInitialSnapshotGenerationEnabled()) {
                sourceRepository.createSources();
                LOGGER.info("Initializing...");
                snapshotFiles.addAll(snapshotFileGenerator.createSnapshots(state));
                LOGGER.info("Initialization complete");
            }
        } else {
            // Must do deltas first since snapshot creation is skipped if there aren't any
            deltaFiles.addAll(deltaFileGenerator.createDeltas(state.serialId()));
            // TODO: is it time to do a snapshot?
            //   snapshotFiles.addAll(snapshotFileGenerator.createSnapshots(state))
        }
        LOGGER.info("NRTM created {} snapshots and {} delta files", snapshotFiles.size(), deltaFiles.size());
        // TODO: optionally create notification file in db...
        //   Get the last notification to see if anything changed now that we might have generated more files
        //   If no snapshot was created for a source, keep the one from the last notification
        //   Get deltas which are < 24 hours old
        //   Don't publish a new one if the files are the same and the last notification is less than a day old
    }

    // Call this from the controller
    public void writeFileToOutput(final String sessionId, final String fileName, final OutputStream out) throws IOException {
        nrtmFileService.writeFileToStream(sessionId, fileName, out);
    }

}
