package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileSerializer;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.InitialSnapshotState;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotObjectSynchronizer snapshotObjectSynchronizer;
    private final SourceRepository sourceRepository;
    private final NrtmFileStore nrtmFileStore;

    public SnapshotFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotObjectSynchronizer snapshotObjectSynchronizer,
        final SourceRepository sourceRepository,
        final NrtmFileStore nrtmFileStore
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotObjectSynchronizer = snapshotObjectSynchronizer;
        this.sourceRepository = sourceRepository;
        this.nrtmFileStore = nrtmFileStore;
    }

    public List<PublishableSnapshotFile> createSnapshots() throws IOException {
        // Get last version from database.
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            final InitialSnapshotState state = snapshotObjectSynchronizer.initializeSnapshotObjects();
            for (final NrtmSourceModel source : sourceRepository.getAllSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                nrtmFileStore.createNrtmSessionDirectory(version.getSessionID());
                sourceVersions.add(version);
            }
        }
        final List<PublishableSnapshotFile> snapshotFiles = new ArrayList<>();
        for (final NrtmVersionInfo sourceVersion : sourceVersions) {
            LOGGER.info("{} at version: {}", sourceVersion.getSource(), sourceVersion);
            if (sourceVersion.getVersion() > 1) {
                LOGGER.debug("Sync Whois changes to snapshot here (not implemented)");
            }
            final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(sourceVersion);
            final String fileName = NrtmFileUtil.newFileName(snapshotFile);
            Stopwatch stopwatch = Stopwatch.createStarted();
            try (final OutputStream out = nrtmFileStore.getFileOutputStream(snapshotFile.getSessionID(), fileName)) {
                snapshotFileSerializer.writeSnapshotAsJson(snapshotFile, out);
            }
            LOGGER.info("Wrote JSON for {} in {}", snapshotFile.getSourceModel(), stopwatch);
            try {
                stopwatch = Stopwatch.createStarted();
                final String sha256hex = DigestUtils.sha256Hex(nrtmFileStore.getFileInputStream(snapshotFile.getSessionID(), fileName));
                snapshotFile.setFileName(fileName);
                snapshotFile.setHash(sha256hex);
                snapshotFileRepository.insert(snapshotFile);
                LOGGER.info("Calculated hash for {} in {}", snapshotFile.getSourceModel(), stopwatch);
                snapshotFiles.add(snapshotFile);
            } catch (final IOException e) {
                LOGGER.error("Exception thrown when calculating hash of snapshot file", e);
                throw e;
            }
        }
        return snapshotFiles;
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSourceModel source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}
