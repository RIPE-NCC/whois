package net.ripe.db.whois.db;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RetryFor(value = IOException.class, attempts = RetryForAspectTest.ATTEMPTS, intervalMs = 0)
class RetryForAspectOnClass {
    public void incrementAndThrowException(final AtomicInteger counter, final Exception e) throws Exception {
        counter.incrementAndGet();
        throw e;
    }
}
