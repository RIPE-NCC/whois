package net.ripe.db.nrtm4;

import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SnapshotRecordProducer implements Supplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotRecordProducer.class);
    private static final int BATCH_SIZE = 100;
    public static final SnapshotFileRecord POISON_PILL = new SnapshotFileRecord(null);
    private final BlockingQueue<SnapshotFileRecord> sharedQueue;
    private final SnapshotState snapshotState;
    private final WhoisObjectRepository whoisObjectRepository;

    private final DummifierNrtmV4 dummifierNrtmV4;

    public SnapshotRecordProducer(final BlockingQueue<SnapshotFileRecord> sharedQueue, final DummifierNrtmV4 dummifierNrtmV4, final SnapshotState snapshotState, final WhoisObjectRepository whoisObjectRepository) {
        this.sharedQueue = sharedQueue;
        this.snapshotState = snapshotState;
        this.whoisObjectRepository = whoisObjectRepository;
        this.dummifierNrtmV4 = dummifierNrtmV4;
    }

    @Override
    public Void get() {

        final AtomicInteger numberOfEnqueuedObjects = new AtomicInteger(0);
        final List<List<WhoisObjectData>> batches = Lists.partition(snapshotState.whoisObjectData(), BATCH_SIZE);
        final int total = snapshotState.whoisObjectData().size();

        final Timer timer = new Timer(true);
        printProgress(numberOfEnqueuedObjects, total, timer);

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

                    try {
                        if (dummifierNrtmV4.isAllowed(rpslObject)) {
                            sharedQueue.put(new SnapshotFileRecord(dummifierNrtmV4.dummify(rpslObject)));
                        }
                    } catch (final InterruptedException e) {
                            LOGGER.error("Interrupted " + rpslObject.getValueForAttribute(AttributeType.SOURCE));
                            Thread.currentThread().interrupt();
                        }
                    }
                });
        } catch (final Exception e) {
            LOGGER.warn("Exception thrown", e);
            Thread.currentThread().interrupt();
        } finally {
            timer.cancel();
            try {
                sharedQueue.put(POISON_PILL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private void printProgress(AtomicInteger numberOfEnqueuedObjects, int total, Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = numberOfEnqueuedObjects.get();
                LOGGER.info("Enqueued {} RPSL objects out of {} ({}%). queue size {}", done, total, Math.round(done * 1000. / total) / 10., sharedQueue.size());
            }
        }, 0, 2000);
    }
}
