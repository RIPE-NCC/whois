package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Repository
public class DeltaFileRepository {

    private final DeltaFileDao deltaFileDao;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;

    DeltaFileRepository(
        final DeltaFileDao deltaFileDao,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository
    ) {
        this.deltaFileDao = deltaFileDao;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
    }

    public void storeDeltasAsPublishableFile(final int serialIDTo, final NrtmVersionInfo version, final List<DeltaChange> deltas) throws JsonProcessingException {
        final NrtmVersionInfo newVersion = nrtmVersionInfoRepository.saveNewDeltaVersion(version, serialIDTo);
        final PublishableDeltaFile publishableDeltaFile = new PublishableDeltaFile(newVersion, deltas);
        final String json = new ObjectMapper().writeValueAsString(publishableDeltaFile);
        final String hash = NrtmFileUtil.calculateSha256(json.getBytes(StandardCharsets.UTF_8));
        final DeltaFile deltaFile = DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json);
        deltaFileDao.save(deltaFile);
    }

}
