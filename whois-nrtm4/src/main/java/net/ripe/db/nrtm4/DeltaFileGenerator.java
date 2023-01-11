package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
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
    private final SerialDao serialDao;
    private final DeltaFileRepository deltaFileRepository;

    public DeltaFileGenerator(
        final DeltaTransformer deltaTransformer,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final DeltaFileRepository deltaFileRepository
    ) {
        this.deltaTransformer = deltaTransformer;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.deltaFileRepository = deltaFileRepository;
    }

    @Transactional("nrtmTransactionManager")
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
        final List<DeltaChange> deltas = deltaTransformer.toDeltaChange(whoisChanges);
        if (deltas.size() < 1) {
            LOGGER.info("Whois changes found but all were filtered");
            return Optional.empty();
        }
        final int lastSerialId = whoisChanges.get(whoisChanges.size() - 1).getSerialId();
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
