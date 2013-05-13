package net.ripe.db.whois.common.aspects;

import java.util.concurrent.atomic.AtomicInteger;

public interface RetryForAspectMethod {
    void incrementAndThrowException(AtomicInteger counter, Exception e) throws Exception;
}
