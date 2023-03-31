package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;

@Repository
public class NrtmFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileRepository.class);

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final DeltaFileDao deltaFileDao;
    private final SnapshotFileRepository snapshotFileRepository;

    public NrtmFileRepository(final SnapshotFileRepository snapshotFileRepository, final NrtmVersionInfoRepository nrtmVersionInfoRepository, final DeltaFileDao deltaFileDao) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.deltaFileDao = deltaFileDao;
        this.snapshotFileRepository = snapshotFileRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDeltaVersion(final NrtmVersionInfo version, final int serialIDTo, final List<DeltaChange> deltas) throws JsonProcessingException {
       if(deltas.isEmpty()) {
         return;
       }

       final NrtmVersionInfo newVersion = nrtmVersionInfoRepository.saveNewDeltaVersion(version, serialIDTo);
       final DeltaFile deltaFile = getDeltaFile(newVersion, deltas);
       deltaFileDao.save(deltaFile.versionId(), deltaFile.name(), deltaFile.hash(), deltaFile.payload());
       LOGGER.info("Created {} delta version {}", newVersion.source().getName(), newVersion.version());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSnapshotVersion(final NrtmVersionInfo version, final String fileName, final String hash, final byte[] payload)  {

        final NrtmVersionInfo newVersion = nrtmVersionInfoRepository.saveNewSnapshotVersion(version);
        final SnapshotFile snapshotFile = SnapshotFile.of(newVersion.id(), fileName, hash);

        snapshotFileRepository.insert(snapshotFile, payload);
        LOGGER.info("Created {} snapshot version {}", version.source().getName(), version.version());
    }

    public DeltaFile getDeltaFile(final NrtmVersionInfo newVersion, final List<DeltaChange> deltas) throws JsonProcessingException {
        final PublishableDeltaFile publishableDeltaFile = new PublishableDeltaFile(newVersion, deltas);
        final String json = new ObjectMapper().writeValueAsString(publishableDeltaFile);
        final String hash = NrtmFileUtil.calculateSha256(json.getBytes(StandardCharsets.UTF_8));
        return DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json);
    }
}
