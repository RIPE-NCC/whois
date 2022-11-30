package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFileModel;
import net.ripe.db.nrtm4.persist.DeltaFileModelRepository;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class NrtmProcessor {

    private final DeltaFileModelRepository deltaFileModelRepository;
    private final DeltaProcessor deltaProcessor;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotSynchronizer snapshotSynchronizer;
    private final SerialDao serialDao;

    public NrtmProcessor(
        final DeltaFileModelRepository deltaFileModelRepository,
        final DeltaProcessor deltaProcessor,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotSynchronizer snapshotSynchronizer,
        final SerialDao serialDao
        ) {
        this.deltaFileModelRepository = deltaFileModelRepository;
        this.deltaProcessor = deltaProcessor;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotSynchronizer = snapshotSynchronizer;
        this.serialDao = serialDao;
    }

    public void initializeSnapshot(final NrtmSource source) {

        // Find whois objects which are in the 'last' table

        // Add them to the snapshot table

    }

    public void validateSnapshot() {

        // Find whois objects which are in the 'last' table

        // Serialize them and compare with what's in the snapshot table -- compare only primary keys?
        // ...or hash?
    }

    public DeltaFileModel generateDeltaFile(final NrtmSource source) {

        // Find changes since the last delta
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        if (lastVersion.isEmpty()) {
            throw new IllegalStateException("Cannot create a delta without an initial snapshot");
        }
        final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesSince(lastVersion.get().getLastSerialId());

        final List<DeltaChange> deltas = deltaProcessor.process(whoisChanges);
        snapshotSynchronizer.synchronizeDeltasToSnapshot(deltas);
        if (whoisChanges.size() < 1) {
            throw new IllegalStateException("Cannot create a delta when there is no previous snapshot");
        }
        final int lastSerialId = whoisChanges.get(whoisChanges.size() - 1).getSerialId();
        final NrtmVersionInfo nextVersion = nrtmVersionInfoRepository.incrementAndSave(lastVersion.get(), lastSerialId);
        final PayloadProcessor processor = new PayloadProcessor(deltas.toArray(new DeltaChange[0]));
        return deltaFileModelRepository.save(
            nextVersion.getId(),
            "nrtm-delta.1.784a2a65aba22e001fd25a1b9e8544e058fbc703.json", // TODO: generate file name for url
            processor.getJson(),
            processor.getHash()
        );
    }

}
