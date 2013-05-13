package net.ripe.db.whois.common.jmx;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

import java.util.concurrent.Callable;

public abstract class JmxBase {
    private final Logger logger;

    public JmxBase(final Logger logger) {
        this.logger = logger;
    }

    public static final String OBJECT_NAME_BASE = "net.ripe.db.whois:name=";

    protected <T> T invokeOperation(final String description, final String comment, final Callable<T> callable) {
        final Stopwatch stopwatch = new Stopwatch().start();

        logger.info("{}: {}", description, comment);

        try {
            return callable.call();
        } catch (Exception e) {
            logger.error("{}: {}", description, comment, e);
            return null;
        } finally {
            logger.info("{}: {} invocation took {}", description, comment, stopwatch.stop());
        }
    }
}
