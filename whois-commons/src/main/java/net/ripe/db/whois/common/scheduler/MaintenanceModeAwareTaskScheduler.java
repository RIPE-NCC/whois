package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.MaintenanceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

public class MaintenanceModeAwareTaskScheduler implements TaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceModeAwareTaskScheduler.class);

    private final TaskScheduler taskScheduler;
    private final MaintenanceMode maintenanceMode;

    public MaintenanceModeAwareTaskScheduler(final TaskScheduler taskScheduler,
                                             final MaintenanceMode maintenanceMode) {
        this.taskScheduler = taskScheduler;
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return taskScheduler.schedule(new MaintenanceModeAwareRunnable(task), trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return taskScheduler.schedule(new MaintenanceModeAwareRunnable(task), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return taskScheduler.scheduleAtFixedRate(new MaintenanceModeAwareRunnable(task), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return taskScheduler.scheduleAtFixedRate(new MaintenanceModeAwareRunnable(task), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return taskScheduler.scheduleWithFixedDelay(new MaintenanceModeAwareRunnable(task), startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return taskScheduler.scheduleWithFixedDelay(new MaintenanceModeAwareRunnable(task), delay);
    }

    private class MaintenanceModeAwareRunnable implements Runnable {

        private final Runnable runnable;

        public MaintenanceModeAwareRunnable(final Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            if (!maintenanceMode.allowUpdate()) {
                LOGGER.info("Scheduled tasks not allowed due to maintenance-mode");
            } else {
                runnable.run();
            }
        }
    };
}
