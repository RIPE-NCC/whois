package net.ripe.db.whois.common.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentState {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentState.class);

    private final BlockingQueue<Boolean> updates = new LinkedBlockingQueue<>();
    private final AtomicBoolean state = new AtomicBoolean();

    public void set(final Boolean value) {
        updates.add(value);
    }

    public void waitUntil(final Boolean value) {
        pollingUpdate();
        while (state.get() != value) {
            LOGGER.info("Waiting for state to be {}", value);
            blockingUpdate();
        }
    }

    private void pollingUpdate() {
        Boolean value;
        while ((value = updates.poll()) != null) {
            state.set(value);
        }
    }

    private void blockingUpdate() {
        try {
            state.set(updates.take());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
