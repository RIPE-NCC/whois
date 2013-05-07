package net.ripe.db.whois.common.pipeline;

import com.google.common.base.Stopwatch;

public final class SafeStopwatch {
    private Stopwatch stopwatch;

    public synchronized boolean start() {
        boolean wasRunning = stopwatch != null;
        stopwatch = new Stopwatch().start();
        return wasRunning;
    }

    public synchronized Long stop() {
        if (stopwatch == null) {
            return null;
        }

        try {
            return stopwatch.stop().elapsedMillis();
        } finally {
            stopwatch = null;
        }
    }
}
