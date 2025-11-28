package net.ripe.db.nrtm4.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoDao;
import net.ripe.db.nrtm4.dao.WhoisObjectDao;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.DeltaFileRecord;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;


@Service
public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final UpdateNrtmFileRepository updateNrtmFileRepository;
    private final DummifierNrtmV4 dummifierNrtmV4;
    private final DateTimeProvider dateTimeProvider;
    private final NrtmVersionInfoDao nrtmVersionInfoDao;
    private final WhoisObjectRepository whoisObjectRepository;
    private final WhoisObjectDao whoisObjectDao;


    DeltaFileGenerator(
            final UpdateNrtmFileRepository updateNrtmFileRepository,
            final DummifierNrtmV4 dummifierNrtmV4,
            final NrtmVersionInfoDao nrtmVersionInfoDao,
            final WhoisObjectDao whoisObjectDao,
            final DateTimeProvider dateTimeProvider,
            final WhoisObjectRepository whoisObjectRepository
    ) {
        this.updateNrtmFileRepository = updateNrtmFileRepository;
        this.dummifierNrtmV4 = dummifierNrtmV4;
        this.nrtmVersionInfoDao = nrtmVersionInfoDao;
        this.whoisObjectRepository = whoisObjectRepository;
        this.whoisObjectDao = whoisObjectDao;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void createDeltas() {

        final int serialIDTo = whoisObjectDao.getLastSerialId();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoDao.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            LOGGER.warn("Seems like snapshot initialization is not done, skipping generating delta files");
            return;
        }

        final Map<CIString, List<DeltaFileRecord>> deltaMap = new HashMap<>();
        sourceVersions.forEach(sv -> deltaMap.put(sv.source().getName(), new ArrayList<>()));

        final List<SerialEntry> whoisChanges = whoisObjectRepository.getSerialEntriesBetween(sourceVersions.get(0).lastSerialId(), serialIDTo);
        LOGGER.debug("Number of objects found between serial id {} - {} is {} ", sourceVersions.get(0).lastSerialId(), serialIDTo, whoisChanges.size());
        // iterate changes and divide objects per source
        for (final SerialEntry serialEntry : whoisChanges) {
            if (!dummifierNrtmV4.isAllowed(serialEntry.getRpslObject())) {
                continue;
            }
            final CIString source = serialEntry.getRpslObject().getValueForAttribute(AttributeType.SOURCE);
            deltaMap.get(source).add(getDeltaChange(serialEntry));
        }

        for (final NrtmVersionInfo version : sourceVersions) {
            try {
                updateNrtmFileRepository.saveDeltaVersion(version, serialIDTo, deltaMap.get(version.source().getName()));
            } catch (final JsonProcessingException e) {
                LOGGER.error("Exception saving delta for {}", version.source().getName(), e);
            }
        }

        LOGGER.debug("Delta file generation completed");

        cleanUpOldFiles();
    }

    private DeltaFileRecord getDeltaChange(final SerialEntry serialEntry) {
        return serialEntry.getOperation() == Operation.DELETE ?
                DeltaFileRecord.delete(serialEntry.getRpslObject().getType().getName(), serialEntry.getPrimaryKey())
                : DeltaFileRecord.addModify(dummifierNrtmV4.dummify(serialEntry.getRpslObject()));
    }

    private void cleanUpOldFiles() {
        LOGGER.debug("Deleting old delta files");

        final LocalDateTime twoDayAgo = dateTimeProvider.getCurrentDateTime().minusDays(2);

        final List<Long> versions = nrtmVersionInfoDao.getAllVersionsByTypeBefore(NrtmDocumentType.DELTA, twoDayAgo);
        updateNrtmFileRepository.deleteDeltaFiles(versions);
    }
}
