package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final DummifierNrtm dummifier;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    DeltaFileGenerator(
        final DummifierNrtm dummifier,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.dummifier = dummifier;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public Set<PublishableDeltaFile> createDeltas(final int serialIDTo) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            throw new RuntimeException("Cannot create deltas on an empty database");
        }
        sourceVersions.sort((v1, v2) -> v2.lastSerialId() - v1.lastSerialId());
        final Map<CIString, List<DeltaChange>> deltas = new HashMap<>();
        sourceVersions.forEach(sv -> deltas.put(sv.source().getName(), List.of()));
        final List<SerialEntry> whoisChanges = whoisObjectRepository.getSerialEntriesBetween(sourceVersions.get(0).lastSerialId(), serialIDTo);
        // iterate changes and divide objects per source
        for (final SerialEntry whoisChange : whoisChanges) {
            if (!dummifier.isAllowed(NRTM_VERSION, whoisChange.getRpslObject())) {
                continue;
            }
            final CIString source = whoisChange.getRpslObject().getValueForAttribute(AttributeType.SOURCE);
            final DeltaChange deltaChange;
            if (whoisChange.getOperation() == Operation.DELETE) {
                deltaChange = DeltaChange.delete(whoisChange.getRpslObject().getType(), whoisChange.getPrimaryKey());
            } else {
                deltaChange = DeltaChange.addModify(dummifier.dummify(NRTM_VERSION, whoisChange.getRpslObject()));
            }
            deltas.get(source).add(deltaChange);
        }
        LOGGER.info("Created delta list {}", stopwatch);
        return Set.of();
    }

}
