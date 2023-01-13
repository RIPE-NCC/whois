package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.ripe.db.nrtm4.dao.DeltaChanges;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.WhoisDao;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final DeltaTransformer deltaTransformer;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final DeltaFileRepository deltaFileRepository;
    private final WhoisDao whoisDao;

    public DeltaFileGenerator(
        final DeltaFileRepository deltaFileRepository,
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisDao whoisDao
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.deltaFileRepository = deltaFileRepository;
        this.whoisDao = whoisDao;
    }

    @Transactional("nrtmTransactionManager")
    public Optional<PublishableDeltaFile> createDelta(final NrtmSource source) {

        // Find changes since the last delta
        final Optional<NrtmVersionInfo> lastVersion = nrtmVersionInfoRepository.findLastVersion(source);
        if (lastVersion.isEmpty()) {
            throw new IllegalStateException("Cannot create a delta without an initial snapshot");
        }
        final DeltaChanges whoisChanges = whoisDao.getDeltasSince(lastVersion.get().getLastSerialId());

        //final List<SerialEntry> whoisChanges = serialDao.getSerialEntriesSince(lastVersion.get().getLastSerialId());
        if (whoisChanges.list().size() < 1) {
            LOGGER.info("No Whois changes found -- delta file generation skipped");
            return Optional.empty();
        }
        final List<DeltaChange> deltas = deltaTransformer.toDeltaChangeList(whoisChanges.list());
        if (deltas.size() < 1) {
            LOGGER.info("Whois changes found but all were filtered");
            return Optional.empty();
        }
        final int lastSerialId = whoisChanges.serialIdTo();
        final NrtmVersionInfo nextVersion = nrtmVersionInfoRepository.incrementAndSave(lastVersion.get(), lastSerialId);
        final PublishableDeltaFile deltaFile = new PublishableDeltaFile(nextVersion, deltas);
        final JsonMapper objectMapper = JsonMapper.builder().build();
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(deltaFile);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final String fileName = NrtmFileUtil.fileName(deltaFile);
        final String sha256hex = DigestUtils.sha256Hex(payload);

        deltaFileRepository.save(
            nextVersion.getId(),
            fileName,
            sha256hex,
            payload
        );
        deltaFile.setFileName(fileName);
        deltaFile.setSha256hex(sha256hex);
        return Optional.of(deltaFile);
    }

}
