package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.generator.SnapshotFileInitializer;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtmv4Condition.class)
public class NrtmInitializeScheduledTask implements DailyScheduledTask {
    private final SnapshotFileInitializer snapshotFileInitializer;

    NrtmInitializeScheduledTask(final SnapshotFileInitializer snapshotFileInitializer) {
        this.snapshotFileInitializer = snapshotFileInitializer;
    }

    @Override
    @Scheduled(cron = "0 */5 1-23 ? * *")
    @SchedulerLock(name = "NrtmSnapshotFileGenerationTask")
    public void run() {
        snapshotFileInitializer.initialize();
    }
}
