package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
class GrsDownloader {
    void acquire(final GrsSource grsSource, final File file, final AcquireHandler acquireHandler) {
        try {
            acquireFile(file, acquireHandler);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to acquire file for source: %s", grsSource), e);
        }
    }

    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 60000)
    void acquireFile(final File file, final AcquireHandler acquireHandler) throws IOException {
        acquireHandler.acquire(file);
    }

    interface AcquireHandler {
        void acquire(File file) throws IOException;
    }
}
