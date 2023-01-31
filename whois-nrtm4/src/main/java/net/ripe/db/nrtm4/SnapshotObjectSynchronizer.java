package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.InitialSnapshotState;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmSourceHolder;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotObject;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class SnapshotObjectSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotObjectSynchronizer.class);
    private static final int BATCH_SIZE = 10;

    private final Dummifier dummifierNrtm;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    SnapshotObjectSynchronizer(
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotObjectRepository snapshotObjectRepository,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    InitialSnapshotState initializeSnapshotObjects() {
        final String method = "initializeSnapshotObjects";
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("{} entered", method);
        final InitialSnapshotState initialState = whoisObjectRepository.getInitialSnapshotState();
        LOGGER.info("{} Found {} objects", method, initialState.rpslObjectData().size());
        LOGGER.info("{} At serial {}, {}ms", method, initialState.serialId(), stopwatch.elapsed().toMillis());
        stopwatch = Stopwatch.createStarted();
        final Map<CIString, NrtmSourceModel> sourceMap = new HashMap<>();
        for (final NrtmSource source: NrtmSourceHolder.getAllSources()) {
            final NrtmSourceModel sourceModel = sourceRepository.createSource(source);
            sourceMap.put(CIString.ciString(source.name()), sourceModel);
        }
        Lists.partition(initialState.rpslObjectData(), BATCH_SIZE)
            .parallelStream()
            .forEach((objectBatch) -> {
                    final List<SnapshotObject> batch = new ArrayList<>(BATCH_SIZE);
                    final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
                    for (final RpslObjectData object : objectBatch) {
                        final String rpsl = rpslMap.get(object.objectId());
                        final RpslObject rpslObject = RpslObject.parse(rpsl);
                        if (!dummifierNrtm.isAllowed(NRTM_VERSION, rpslObject)) {
                            continue;
                        }
                        final RpslObject dummyRpsl = dummifierNrtm.dummify(NRTM_VERSION, rpslObject);
                        final NrtmSourceModel source = sourceMap.get(dummyRpsl.getValueForAttribute(AttributeType.SOURCE));
                        batch.add(new SnapshotObject(0, source.id(), object.objectId(), object.sequenceId(), dummyRpsl));
                    }
                    snapshotObjectRepository.batchInsert(batch);
                }
            );
        LOGGER.info("{} Complete. Initial snapshot objects took {} min", method, stopwatch.elapsed());
        return initialState;
    }

}
