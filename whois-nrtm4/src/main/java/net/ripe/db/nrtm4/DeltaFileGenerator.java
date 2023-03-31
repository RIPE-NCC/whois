package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectDao;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final NrtmFileRepository nrtmFileRepository;
    private final DummifierNrtm dummifier;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final WhoisObjectRepository whoisObjectRepository;
    private final WhoisObjectDao whoisObjectDao;


    DeltaFileGenerator(
        final NrtmFileRepository nrtmFileRepository,
        final DummifierNrtm dummifier,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisObjectDao whoisObjectDao,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.nrtmFileRepository = nrtmFileRepository;
        this.dummifier = dummifier;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.whoisObjectRepository = whoisObjectRepository;
        this.whoisObjectDao = whoisObjectDao;
    }

    public void createDeltas() {

        final int serialIDTo = whoisObjectDao.getLastSerialId();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            LOGGER.warn("Seems like snapshot initialization is not done, skipping generating delta files");
            return;
        }

        final Map<CIString, List<DeltaChange>> deltaMap = new HashMap<>();
        sourceVersions.forEach(sv -> deltaMap.put(sv.source().getName(), new ArrayList<>()));

        final List<SerialEntry> whoisChanges = whoisObjectRepository.getSerialEntriesBetween(sourceVersions.get(0).lastSerialId(), serialIDTo);
        // iterate changes and divide objects per source
        for (final SerialEntry serialEntry : whoisChanges) {
            if (!dummifier.isAllowed(NRTM_VERSION, serialEntry.getRpslObject())) {
                continue;
            }
            final CIString source = serialEntry.getRpslObject().getValueForAttribute(AttributeType.SOURCE);
            deltaMap.get(source).add(getDeltaChange(serialEntry));
        }

        for (final NrtmVersionInfo version : sourceVersions) {
            try {
                nrtmFileRepository.saveDeltaVersion(version, serialIDTo, deltaMap.get(version.source().getName()));
            } catch (final JsonProcessingException e) {
               LOGGER.error("Exception saving delta for {}", version.source().getName(), e);
            }
        }
    }

    private DeltaChange getDeltaChange(final SerialEntry serialEntry) {
        return serialEntry.getOperation() == Operation.DELETE ?
                DeltaChange.delete(serialEntry.getRpslObject().getType(), serialEntry.getPrimaryKey())
                : DeltaChange.addModify(dummifier.dummify(NRTM_VERSION, serialEntry.getRpslObject()));
    }
}
