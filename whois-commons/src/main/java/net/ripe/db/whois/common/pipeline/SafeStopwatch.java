package net.ripe.db.whois.common.pipeline;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public final class SafeStopwatch {
    private Stopwatch stopwatch;

    public synchronized boolean start() {
        boolean wasRunning = stopwatch != null;
        stopwatch = Stopwatch.createStarted();
        return wasRunning;
    }

    public synchronized Long stop() {
        if (stopwatch == null) {
            return null;
        }

        try {
            return stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        } finally {
            stopwatch = null;
        }
    }
}
