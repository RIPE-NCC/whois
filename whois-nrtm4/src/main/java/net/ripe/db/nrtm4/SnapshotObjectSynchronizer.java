package net.ripe.db.nrtm4;

import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.dao.InitialSnapshotState;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.ObjectData;
import net.ripe.db.nrtm4.dao.SnapshotObject;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.nrtm4.dao.WhoisDao;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private final WhoisDao whoisDao;

    SnapshotObjectSynchronizer(
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotObjectRepository snapshotObjectRepository,
        final WhoisDao whoisDao
    ) {
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.whoisDao = whoisDao;
    }

    NrtmVersionInfo initializeSnapshotObjects(final NrtmSource source) {
        final String method = "initializeSnapshotObjects";
        long mark = System.currentTimeMillis();
        LOGGER.info("{} entered", method);
        final InitialSnapshotState initialState = whoisDao.getInitialSnapshotState();
        LOGGER.info("{} Found {} objects", method, initialState.objectData().size());
        LOGGER.info("{} At serial {}, {}ms", method, initialState.serialId(), (System.currentTimeMillis() - mark));
        mark = System.currentTimeMillis();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, initialState.serialId());
        Lists.partition(initialState.objectData(), BATCH_SIZE)
            .parallelStream()
            .forEach((objectBatch) -> {
                    final List<SnapshotObject> batch = new ArrayList<>(BATCH_SIZE);
                    final Map<Integer, String> rpslMap = whoisDao.findRpslMapForObjects(objectBatch);
                    for (final ObjectData object : objectBatch) {
                        final String rpsl = rpslMap.get(object.objectId());
                        final RpslObject rpslObject = RpslObject.parse(rpsl);
                        if (!dummifierNrtm.isAllowed(NRTM_VERSION, rpslObject)) {
                            continue;
                        }
                        final RpslObject dummyRpsl = dummifierNrtm.dummify(NRTM_VERSION, rpslObject);
                        batch.add(new SnapshotObject(0, version.getId(), object.objectId(), object.sequenceId(), dummyRpsl));
                    }
                    snapshotObjectRepository.batchInsert(batch);
                }
            );
        final DecimalFormat df = new DecimalFormat("#,###.000");
        LOGGER.info("{} Complete. Initial snapshot objects took {} min", method, df.format((System.currentTimeMillis() - mark) / 60000));
        return version;
    }

}
