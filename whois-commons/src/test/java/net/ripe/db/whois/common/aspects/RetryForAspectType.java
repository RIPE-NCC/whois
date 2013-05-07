package net.ripe.db.whois.common.aspects;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public interface RetryForAspectType {
    @RetryFor(value = IOException.class, attempts = RetryForAspectTest.ATTEMPTS, intervalMs = 0)
    void incrementAndThrowException(AtomicInteger counter, Exception e) throws Exception;
}
