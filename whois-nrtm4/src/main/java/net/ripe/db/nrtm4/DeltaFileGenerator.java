package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFile;
import net.ripe.db.nrtm4.persist.DeltaFileRepository;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
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
    private final SerialDao serialDao;
    private final NrtmFileRepo nrtmFileRepo;
    private final DeltaFileRepository deltaFileRepository;

    public DeltaFileGenerator(
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final NrtmFileRepo nrtmFileRepo,
        final DeltaFileRepository deltaFileRepository
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.nrtmFileRepo = nrtmFileRepo;
        this.deltaFileRepository = deltaFileRepository;
    }

    public PublishableDeltaFile createDelta(
        final NrtmSource source,
        final long versionNumber
    ) {
        final Optional<NrtmVersionInfo> versionInfoOptional = nrtmVersionInfoRepository.findVersionNumber(source, versionNumber);
        if (versionInfoOptional.isEmpty()) {
            throw new IllegalStateException("version has not been published: " + versionNumber);
        }
        final NrtmVersionInfo version = versionInfoOptional.get();
        // see if file exists locally and return it if so
        final DeltaFile deltaFile = deltaFileRepository.getByVersionId(version.getId());
        if (nrtmFileRepo.checkIfFileExists(deltaFile.getName())) {
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

    public Optional<PublishableDeltaFile> createDelta(final NrtmSource source) {

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
        return Optional.of(deltaFile);
    }

}
