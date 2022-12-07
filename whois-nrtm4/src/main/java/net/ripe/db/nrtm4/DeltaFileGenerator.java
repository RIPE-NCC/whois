package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.PublishedFile;
import net.ripe.db.nrtm4.persist.PublishedFileRepository;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final DeltaTransformer deltaTransformer;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotSynchronizer snapshotSynchronizer;
    private final SerialDao serialDao;
    private final NrtmFileRepo nrtmFileRepo;
    private final PublishedFileRepository publishedFileRepository;

    public DeltaFileGenerator(
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotSynchronizer snapshotSynchronizer,
        final SerialDao serialDao,
        final NrtmFileRepo nrtmFileRepo,
        final PublishedFileRepository publishedFileRepository
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotSynchronizer = snapshotSynchronizer;
        this.serialDao = serialDao;
        this.nrtmFileRepo = nrtmFileRepo;
        this.publishedFileRepository = publishedFileRepository;
    }

    public void initializeSnapshot(final NrtmSource source) {

        // Find whois objects which are in the 'last' table
        final List<SerialEntry> allObjects = serialDao.getSerialEntriesFromLast();

        // Add them to the snapshot table

    }

    public void validateSnapshot() {

        // Find whois objects which are in the 'last' table

        // Serialize them and compare with what's in the snapshot table -- compare only primary keys?
        // ...or hash?
    }

    public PublishableDeltaFile processDeltas(
        final NrtmSource source,
        final long versionNumber
    ) {
        final Optional<NrtmVersionInfo> versionInfoOptional = nrtmVersionInfoRepository.findVersionNumber(source, versionNumber);
        if (versionInfoOptional.isEmpty()) {
            throw new IllegalStateException("version has not been published: " + versionNumber);
        }
        final NrtmVersionInfo version = versionInfoOptional.get();
        // see if file exists locally and return it if so
        final PublishedFile publishedFile = publishedFileRepository.getByTypeAndVersionId(NrtmDocumentType.DELTA, version.getId());
        if (nrtmFileRepo.checkIfFileExists(publishedFile.getName())) {
            // get it and return it
            return null;
        }

        // otherwise generate a new one
        final Optional<NrtmVersionInfo> lastVersionInfoOptional = nrtmVersionInfoRepository.findVersionNumber(source, versionNumber - 1);
        if (lastVersionInfoOptional.isEmpty()) {
            throw new IllegalStateException("earlier version is missing: " + (versionNumber - 1));
        }
        return null;
    }

    public Optional<PublishableDeltaFile> processDeltas(final NrtmSource source) {

        // Find changes since the last delta
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        if (lastVersion.isEmpty()) {
            throw new IllegalStateException("Cannot create a delta without an initial snapshot");
        }
        final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesSince(lastVersion.get().getLastSerialId());
        if (whoisChanges.size() < 1) {
            LOGGER.info("No Whois changes found -- delta file generation skipped");
            return Optional.empty();
        }
        final List<DeltaChange> deltas = deltaTransformer.process(whoisChanges);
        final int lastSerialId = whoisChanges.get(whoisChanges.size() - 1).getSerialId();
        final NrtmVersionInfo nextVersion = nrtmVersionInfoRepository.incrementAndSave(lastVersion.get(), lastSerialId);
        final PublishableDeltaFile deltaFile = new PublishableDeltaFile(nextVersion);
        deltaFile.setChanges(deltas);
        snapshotSynchronizer.synchronizeDeltasToSnapshot(deltaFile);
        return Optional.of(deltaFile);
    }

}
