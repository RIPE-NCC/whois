package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.ObjectData;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


@Service
public class RpslObjectBatchEnqueuer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectBatchEnqueuer.class);
    private static final int BATCH_SIZE = 10;

    private final WhoisObjectRepository whoisObjectRepository;

    RpslObjectBatchEnqueuer(
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisObjectRepository = whoisObjectRepository;
    }

    void enrichAndEnqueueRpslObjects(final SnapshotState snapshotState, final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap) throws InterruptedException {
        LOGGER.info("enrichAndEnqueueRpslObjects entered");
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<List<ObjectData>> batches = Lists.partition(snapshotState.objectData(), BATCH_SIZE);
        for (final List<ObjectData> objectBatch : batches) {
            final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
            for (final ObjectData object : objectBatch) {
                final String rpsl = rpslMap.get(object.objectId());
                final RpslObject rpslObject = RpslObject.parse(rpsl);
                final LinkedBlockingQueue<RpslObjectData> source = queueMap.get(rpslObject.getValueForAttribute(AttributeType.SOURCE));
                if (source == null) {
                    final String msg = "RPSL object declares an unknown source attribute";
                    LOGGER.error(msg + " " + rpslObject.getValueForAttribute(AttributeType.SOURCE) + " known: " + Arrays.toString(queueMap.keySet().toArray()));
                    throw new NrtmDataInconsistencyException(msg);
                }
                source.put(new RpslObjectData(object.objectId(), object.sequenceId(), rpslObject));
            }
        }
        for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
            queue.put(new RpslObjectData(0, 0, null));
        }
        LOGGER.info("Snapshot objects iterated in {}", stopwatch);
    }

}
