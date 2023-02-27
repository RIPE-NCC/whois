package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.jmx.NrtmProcessControl;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


@Service
public class NrtmFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileProcessor.class);

    private final NrtmFileService nrtmFileService;
    private final NrtmProcessControl nrtmProcessControl;
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final SourceRepository sourceRepository;

    public NrtmFileProcessor(
        final NrtmFileService nrtmFileService,
        final NrtmProcessControl nrtmProcessControl,
        final SnapshotFileGenerator snapshotFileGenerator,
        final SourceRepository sourceRepository
    ) {
        this.nrtmFileService = nrtmFileService;
        this.nrtmProcessControl = nrtmProcessControl;
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.sourceRepository = sourceRepository;
    }

    public void updateNrtmFilesAndPublishNotification() throws IOException {
        LOGGER.info("updateNrtmFilesAndPublishNotification() called");
        final List<NrtmSourceModel> sourceList = sourceRepository.getAllSources();
        if (sourceList.isEmpty()) {
            if (nrtmProcessControl.isInitialSnapshotGenerationEnabled()) {
                sourceRepository.createSources();
                LOGGER.info("Initializing...");
                snapshotFileGenerator.createSnapshots();
                LOGGER.info("Initialization complete");
            } else {
                LOGGER.info("Initialization skipped because NrtmProcessControl has disabled initial snapshot generation");
            }
        }
        // TODO: optionally create notification file in db
        // - Get the last notification to see if anything changed now that we might have generated more files
        // - if publishableSnapshotFile is empty, keep the one from the last notification
        // - get deltas which are < 24 hours old
        // - don't publish a new one if the files are the same and the last notification is less than a day old
    }

    // Call this from the controller
    public void writeFileToOutput(final String sessionId, final String fileName, final OutputStream out) throws IOException {
        nrtmFileService.writeFileToStream(sessionId, fileName, out);
    }

}
