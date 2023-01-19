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
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class SnapshotObjectSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotObjectSynchronizer.class);

    private final int batchSize;
    private final Dummifier dummifierNrtm;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final WhoisDao whoisDao;

    SnapshotObjectSynchronizer(
        @Value("${nrtm.snapshot.insert.batchSize:10}") final int batchSize,
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        @Qualifier("whoisSlaveSerialDao") final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository,
        final WhoisDao whoisDao
    ) {
        this.batchSize = batchSize;
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
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
        Lists.partition(initialState.objectData(), batchSize)
            .parallelStream()
            .forEach((objectBatch) -> {
                    final List<SnapshotObject> batch = new ArrayList<>(batchSize);
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
