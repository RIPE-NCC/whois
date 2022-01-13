package net.ripe.db.whois.api.httpserver;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.ReadinessUpdater;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DelayShutdownHook extends AbstractLifeCycle {

    @Value("${shutdown.pause.sec:10}")
    private int preShutdownPause;

    private final ReadinessUpdater readinessUpdater;


    @Autowired
    public DelayShutdownHook(ReadinessUpdater readinessUpdater) {
        this.readinessUpdater = readinessUpdater;
    }

    public void register() {
        try {
            start(); // lifecycle must be started in order for stop() to be called
            // register the shutdown handler as first (index 0) so that it executes before Jetty's shutdown behavior
            ShutdownThread.register(0, this);
        } catch (Exception e) {
            throw new IllegalStateException("failed setting up delayed shutdown handler", e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        readinessUpdater.up();
    }

    @Override
    protected void doStop() throws Exception {
        readinessUpdater.down();
        Uninterruptibles.sleepUninterruptibly(preShutdownPause, TimeUnit.SECONDS);
        super.doStop();
    }
}
