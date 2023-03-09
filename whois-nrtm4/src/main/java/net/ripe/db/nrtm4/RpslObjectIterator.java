package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;
import static net.ripe.db.nrtm4.RpslObjectEnqueuer.POISON_PILL;


public class RpslObjectIterator implements Iterator<RpslObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectIterator.class);

    private final Dummifier dummifier;
    private final LinkedBlockingQueue<RpslObjectData> queue;
    private RpslObject next;

    RpslObjectIterator(
        final Dummifier dummifier,
        final LinkedBlockingQueue<RpslObjectData> queue
    ) {
        this.dummifier = dummifier;
        this.queue = queue;
    }

    @Override
    public boolean hasNext() {
        try {
            while(true) {
                final RpslObjectData rpslObjectData = queue.take();
                if (rpslObjectData.objectId() == POISON_PILL.objectId()) {
                    next = null;
                    return false;
                }
                if (dummifier.isAllowed(NRTM_VERSION, rpslObjectData.rpslObject())) {
                    next = dummifier.dummify(NRTM_VERSION, rpslObjectData.rpslObject());
                    return true;
                }
            }
        } catch (final InterruptedException e) {
            LOGGER.warn("Iterator interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public RpslObject next() {
        return next;
    }

}
