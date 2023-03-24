package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
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

    private final DeltaFileRepository deltaFileRepository;
    private final DummifierNrtm dummifier;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    DeltaFileGenerator(
        final DeltaFileRepository deltaFileRepository,
        final DummifierNrtm dummifier,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.deltaFileRepository = deltaFileRepository;
        this.dummifier = dummifier;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public void createDeltas(final int serialIDTo) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            throw new RuntimeException("Cannot create deltas on an empty database");
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
            final DeltaChange deltaChange;
            if (serialEntry.getOperation() == Operation.DELETE) {
                deltaChange = DeltaChange.delete(serialEntry.getRpslObject().getType(), serialEntry.getPrimaryKey());
            } else {
                deltaChange = DeltaChange.addModify(dummifier.dummify(NRTM_VERSION, serialEntry.getRpslObject()));
            }
            deltaMap.get(source).add(deltaChange);
        }
        for (final NrtmVersionInfo version : sourceVersions) {
            final List<DeltaChange> deltas = deltaMap.get(version.source().getName());
            if (!deltas.isEmpty()) {
                try {
                    final NrtmVersionInfo newVersion = nrtmVersionInfoRepository.saveNewDeltaVersion(version, serialIDTo);
                    deltaFileRepository.storeDeltasAsPublishableFile(newVersion, deltas);
                    LOGGER.info("Created {} delta version {} in {}", newVersion.source().getName(), newVersion.version(), stopwatch);
                } catch (final JsonProcessingException e) {
                    LOGGER.error("Exception saving delta for {}", version.source().getName(), e);
                }
            }
        }
    }

}
