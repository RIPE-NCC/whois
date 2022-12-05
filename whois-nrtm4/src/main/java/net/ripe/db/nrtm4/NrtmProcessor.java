package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.PublishedFileRepository;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class NrtmProcessor {

    private final PublishedFileRepository publishedFileRepository;
    private final DeltaTransformer deltaTransformer;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotSynchronizer snapshotSynchronizer;
    private final SerialDao serialDao;

    public NrtmProcessor(
        final PublishedFileRepository publishedFileRepository,
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotSynchronizer snapshotSynchronizer,
        final SerialDao serialDao
        ) {
        this.publishedFileRepository = publishedFileRepository;
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotSynchronizer = snapshotSynchronizer;
        this.serialDao = serialDao;
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

    public PublishableDeltaFile processDeltas(final NrtmSource source) {

        // Find changes since the last delta
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        if (lastVersion.isEmpty()) {
            throw new IllegalStateException("Cannot create a delta without an initial snapshot");
        }
        final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesSince(lastVersion.get().getLastSerialId());
        if (whoisChanges.size() < 1) {
            throw new IllegalStateException("Cannot create a delta when there is no previous snapshot");
        }
        final List<DeltaChange> deltas = deltaTransformer.process(whoisChanges);
        snapshotSynchronizer.synchronizeDeltasToSnapshot(deltas);
        final int lastSerialId = whoisChanges.get(whoisChanges.size() - 1).getSerialId();
        final NrtmVersionInfo nextVersion = nrtmVersionInfoRepository.incrementAndSave(lastVersion.get(), lastSerialId);
        final PublishableDeltaFile deltaFile = new PublishableDeltaFile(nextVersion);
        deltaFile.setChanges(deltas);
        return deltaFile;
    }

}
