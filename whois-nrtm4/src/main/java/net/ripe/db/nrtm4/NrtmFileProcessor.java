package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmSourceHolder;
import net.ripe.db.nrtm4.dao.SnapshotFile;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.util.SnapshotUpdateWindow;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;


@Service
public class NrtmFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileProcessor.class);

    private final DeltaFileGenerator deltaFileGenerator;
    private final NrtmFileService nrtmFileService;
    private final NrtmSourceHolder nrtmSourceHolder;
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final SnapshotUpdateWindow snapshotUpdateWindow;

    public NrtmFileProcessor(
        final DeltaFileGenerator deltaFileGenerator,
        final NrtmFileService nrtmFileService,
        final NrtmSourceHolder nrtmSourceHolder,
        final SnapshotFileGenerator snapshotFileGenerator,
        final SnapshotUpdateWindow snapshotUpdateWindow
    ) {
        this.deltaFileGenerator = deltaFileGenerator;
        this.nrtmFileService = nrtmFileService;
        this.nrtmSourceHolder = nrtmSourceHolder;
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.snapshotUpdateWindow = snapshotUpdateWindow;
    }

    @Scheduled(fixedDelay = 60 * 1_000)
    public void runRead() {
        // get latest notification

        // call this for each file referenced...
        //nrtmFileService.syncNrtmFileToFileSystem(name);
    }

    @Transactional
    public void runWrite() {
        LOGGER.info("runWrite() called");
        final NrtmSource source = nrtmSourceHolder.getSource();
        final Optional<SnapshotFile> snapshotFile = snapshotFileGenerator.getLastSnapshot(source);
        Optional<PublishableSnapshotFile> publishableSnapshotFile;
        if (snapshotFile.isEmpty()) {
            LOGGER.info("No previous snapshot found. Initializing...");
            publishableSnapshotFile = snapshotFileGenerator.createSnapshot(source);
            LOGGER.info("Initialization done.");
        } else {
            final Optional<PublishableDeltaFile> optDelta = deltaFileGenerator.createDelta(source);
            if (snapshotUpdateWindow.fileShouldBeUpdated(snapshotFile.get())) {
                LOGGER.info("Generating a new snapshot...");
                publishableSnapshotFile = snapshotFileGenerator.createSnapshot(source);
                LOGGER.info("Generating snapshot done");
            }
        }
        // TODO: optionally create notification file in db
        // - Get the last notification to see if anything changed
        // - if publishableSnapshotFile is null or empty, keep the one from the notification
        // - get deltas which are < 24 hours old
        // - don't publish a new one if the files are the same and the last notification is less than a day old
    }

    // Call this from the controller
    public void writeFileToOutput(final String sessionId, final String fileName, final OutputStream out) throws IOException {
        nrtmFileService.writeFileToStream(fileName, out);
    }

}
