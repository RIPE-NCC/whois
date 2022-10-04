package net.ripe.db.whois.api.httpserver;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.ReadinessHealthCheck;
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

    private final ReadinessHealthCheck readinessHealthCheck;

    @Autowired
    public DelayShutdownHook(final ReadinessHealthCheck readinessHealthCheck) {
        this.readinessHealthCheck = readinessHealthCheck;
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
        readinessHealthCheck.up();
    }

    @Override
    protected void doStop() throws Exception {
        readinessHealthCheck.down();
        Uninterruptibles.sleepUninterruptibly(preShutdownPause, TimeUnit.SECONDS);
        super.doStop();
    }
}
