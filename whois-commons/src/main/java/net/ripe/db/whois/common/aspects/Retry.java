package net.ripe.db.whois.common.aspects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Retry {
    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class);

    public static <T> T forExceptions(final Retryable<T> retryable, final int maxAttempts, final long sleepMsBetweenRetries, final Class<? extends Exception>... exceptionClasses) throws Exception {
        int attempt = 0;
        Exception originalException = null;

        while (true) {
            try {
                return retryable.attempt();
            } catch (Error e) {
                throw e;
            } catch (Exception e) {
                if (isRetryAfterException(e, exceptionClasses)) {
                    if (originalException == null) {
                        originalException = e;
                    }

                    if (++attempt < maxAttempts) {
                        LOGGER.error("{} attempt {}/{} failed, retrying in {} ms", retryable.getClass().getName(), attempt, maxAttempts, sleepMsBetweenRetries, e);
                        Thread.sleep(sleepMsBetweenRetries);
                    } else {
                        LOGGER.error("{} attempt {}/{} failed", retryable.getClass().getName(), attempt, maxAttempts, e);
                        throw originalException;
                    }
                } else {
                    throw e;
                }
            } catch (Throwable e) {
                throw new IllegalStateException("Unexpected throwable", e);
            }
        }
    }

    private static boolean isRetryAfterException(final Exception e, final Class<? extends Exception>... exceptionClasses) {
        for (final Class<? extends Exception> exceptionClass : exceptionClasses) {
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                return true;
            }
        }

        return false;
    }

    public interface Retryable<T> {
        T attempt() throws Throwable;
    }
}
