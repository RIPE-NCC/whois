package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmSourceHolder;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.jmx.NrtmProcessControl;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;


@Service
public class NrtmFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileProcessor.class);

    private final NrtmFileService nrtmFileService;
    private final NrtmProcessControl nrtmProcessControl;
    private final NrtmSourceHolder nrtmSourceHolder;
    private final SnapshotFileGenerator snapshotFileGenerator;

    public NrtmFileProcessor(
        final NrtmFileService nrtmFileService,
        final NrtmProcessControl nrtmProcessControl,
        final NrtmSourceHolder nrtmSourceHolder,
        final SnapshotFileGenerator snapshotFileGenerator
    ) {
        this.nrtmFileService = nrtmFileService;
        this.nrtmProcessControl = nrtmProcessControl;
        this.nrtmSourceHolder = nrtmSourceHolder;
        this.snapshotFileGenerator = snapshotFileGenerator;
    }

    public void updateNrtmFilesAndPublishNotification() throws IOException {
        LOGGER.info("runWrite() called");
        final NrtmSource source = nrtmSourceHolder.getSource();
        final Optional<SnapshotFile> lastSnapshot = snapshotFileGenerator.getLastSnapshot(source);
        List<PublishableSnapshotFile> publishableSnapshotFileList;
        if (lastSnapshot.isEmpty()) {
            LOGGER.info("No previous snapshot found");
            if (nrtmProcessControl.isInitialSnapshotGenerationEnabled()) {
                LOGGER.info("Initializing...");
                publishableSnapshotFileList = snapshotFileGenerator.createSnapshots();
                LOGGER.info("Initialization complete");
            } else {
                LOGGER.info("Initialization skipped because NrtmProcessControl has disabled initial snapshot generation");
                return;
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
