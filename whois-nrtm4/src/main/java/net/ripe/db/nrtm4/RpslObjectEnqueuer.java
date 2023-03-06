package net.ripe.db.nrtm4;

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
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class RpslObjectEnqueuer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectEnqueuer.class);
    private static final int BATCH_SIZE = 100;
    public static final RpslObjectData POISON_PILL = new RpslObjectData(0, 0, null);

    private final WhoisObjectRepository whoisObjectRepository;
    private final AtomicInteger numberOfEnqueuedObjects;

    RpslObjectEnqueuer(
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisObjectRepository = whoisObjectRepository;
        numberOfEnqueuedObjects = new AtomicInteger(0);
    }

    void enrichAndEnqueueRpslObjects(final SnapshotState snapshotState, final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap) throws InterruptedException {
        final List<List<ObjectData>> batches = Lists.partition(snapshotState.objectData(), BATCH_SIZE);
        numberOfEnqueuedObjects.set(0);
        try {
            //for (final List<ObjectData> objectBatch : batches) {
            batches.parallelStream().forEach(objectBatch -> {
                final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
                for (final ObjectData object : objectBatch) {
                    numberOfEnqueuedObjects.incrementAndGet();
                    final String rpsl = rpslMap.get(object.objectId());
                    final RpslObject rpslObject = RpslObject.parse(rpsl);
                    final LinkedBlockingQueue<RpslObjectData> queue = queueMap.get(rpslObject.getValueForAttribute(AttributeType.SOURCE));
                    if (queue == null) {
                        final String msg = "RPSL object declares an unknown source attribute";
                        LOGGER.error(msg + " " + rpslObject.getValueForAttribute(AttributeType.SOURCE) + " known: " + Arrays.toString(queueMap.keySet().toArray()));
                        throw new NrtmDataInconsistencyException(msg);
                    }
                    try {
                        queue.put(new RpslObjectData(object.objectId(), object.sequenceId(), rpslObject));
                    } catch (final InterruptedException e) {
                        LOGGER.error("Interrupted " + rpslObject.getValueForAttribute(AttributeType.SOURCE));
                        throw new RuntimeException(e);
                    }
                }
            });
            for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
                queue.put(POISON_PILL);
            }
        } catch (final Exception e) {
            for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
                try {
                    queue.put(POISON_PILL);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            throw e;
        }
    }

    int getDoneCount() {
        return numberOfEnqueuedObjects.get();
    }
}
