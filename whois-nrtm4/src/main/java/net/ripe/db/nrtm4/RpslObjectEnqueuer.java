package net.ripe.db.nrtm4;

import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class RpslObjectEnqueuer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectEnqueuer.class);
    private static final int BATCH_SIZE = 100;
    public static final RpslObjectData POISON_PILL = new RpslObjectData(0, 0, null);

    private final String whoisSource;
    private final WhoisObjectRepository whoisObjectRepository;

    RpslObjectEnqueuer(
        @Value("${whois.source}") final String whoisSource,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisSource = whoisSource;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    RpslObjectQueueRunner getRunner(
        final SnapshotState snapshotState,
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap
    ) {
        return new RpslObjectQueueRunner(
            whoisObjectRepository,
            snapshotState,
            queueMap,
            CIString.ciString(whoisSource)
        );
    }

    private record RpslObjectQueueRunner(
        WhoisObjectRepository whoisObjectRepository, SnapshotState snapshotState,
        Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap,
        CIString whoisSource
    ) implements Runnable {

        public void run() {
            final AtomicInteger numberOfEnqueuedObjects = new AtomicInteger(0);
            final List<List<WhoisObjectData>> batches = Lists.partition(snapshotState.whoisObjectData(), BATCH_SIZE);
            final int total = snapshotState.whoisObjectData().size();
            final Timer timer = new Timer(true);
            final LinkedBlockingQueue<RpslObjectData> whoisQueue = queueMap.get(whoisSource);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    final int done = numberOfEnqueuedObjects.get();
                    LOGGER.info("Enqueued {} RPSL objects out of {} ({}%). {} queue size {}", done, total, Math.round(done * 1000. / total) / 10., whoisSource, whoisQueue.size());
                }
            }, 0, 2000);

            try {
                batches.parallelStream().forEach(objectBatch -> {
                    final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
                    for (final WhoisObjectData object : objectBatch) {
                        numberOfEnqueuedObjects.incrementAndGet();
                        final String rpsl = rpslMap.get(object.objectId());
                        final RpslObject rpslObject;
                        try {
                            rpslObject = RpslObject.parse(rpsl);
                        } catch (final Exception e) {
                            LOGGER.warn("Parsing RPSL threw exception", e);
                            continue;
                        }
                        final LinkedBlockingQueue<RpslObjectData> queue = queueMap.get(rpslObject.getValueForAttribute(AttributeType.SOURCE));
                        if (queue != null) {
                            try {
                                queue.put(new RpslObjectData(object.objectId(), object.sequenceId(), rpslObject));
                            } catch (final InterruptedException e) {
                                LOGGER.error("Interrupted " + rpslObject.getValueForAttribute(AttributeType.SOURCE));
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                });
            } catch (final Exception e) {
                LOGGER.warn("Exception thrown", e);
                Thread.currentThread().interrupt();
            } finally {
                timer.cancel();
                for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
                    try {
                        queue.put(POISON_PILL);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

    }

}
