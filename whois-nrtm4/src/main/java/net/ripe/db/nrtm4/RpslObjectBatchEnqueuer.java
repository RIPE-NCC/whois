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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class RpslObjectBatchEnqueuer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectBatchEnqueuer.class);
    private static final int BATCH_SIZE = 10;
    public static final RpslObjectData POISON_PILL = new RpslObjectData(0, 0, null);

    private final WhoisObjectRepository whoisObjectRepository;
    private final CIString whoisSource;

    RpslObjectBatchEnqueuer(
        final WhoisObjectRepository whoisObjectRepository,
        @Value("${whois.source}") final String whoisSource
    ) {
        this.whoisObjectRepository = whoisObjectRepository;
        this.whoisSource = CIString.ciString(whoisSource);
    }

    void enrichAndEnqueueRpslObjects(final SnapshotState snapshotState, final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap) {
        LOGGER.info("enrichAndEnqueueRpslObjects entered");
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<List<ObjectData>> batches = Lists.partition(snapshotState.objectData(), BATCH_SIZE);
        final int total = snapshotState.objectData().size();
        final AtomicInteger complete = new AtomicInteger(0);
        final AtomicInteger queueSize = new AtomicInteger(0);
        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = complete.get();
                LOGGER.info("NRTM RpslQueue {} of {} ({}%). Queue size {}", done, total, Math.round((float) (done * 100) / (float) total), queueSize.get());
            }
        }, 0, 2000);
        try {
            for (final List<ObjectData> objectBatch : batches) {
                final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
                for (final ObjectData object : objectBatch) {
                    complete.incrementAndGet();
                    final String rpsl = rpslMap.get(object.objectId());
                    final RpslObject rpslObject = RpslObject.parse(rpsl);
                    final LinkedBlockingQueue<RpslObjectData> queue = queueMap.get(rpslObject.getValueForAttribute(AttributeType.SOURCE));
                    if (queue == null) {
                        final String msg = "RPSL object declares an unknown source attribute";
                        LOGGER.error(msg + " " + rpslObject.getValueForAttribute(AttributeType.SOURCE) + " known: " + Arrays.toString(queueMap.keySet().toArray()));
                        throw new NrtmDataInconsistencyException(msg);
                    }
                    queue.put(new RpslObjectData(object.objectId(), object.sequenceId(), rpslObject));
                    queueSize.set(queueMap.get(whoisSource).size());
                }
            }
            for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
                queue.put(POISON_PILL);
            }
        } catch (final Exception e) {
            for (final LinkedBlockingQueue<RpslObjectData> queue : queueMap.values()) {
                try {
                    timer.cancel();
                    queue.put(POISON_PILL);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        LOGGER.info("Snapshot objects iterated in {}", stopwatch);
    }

}
