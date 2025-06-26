package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.generator.SnapshotFileGenerator;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtmv4Condition.class)
public class SnapshotFileScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileScheduledTask.class);
    private final SnapshotFileGenerator snapshotFileGenerator;

    SnapshotFileScheduledTask(final SnapshotFileGenerator snapshotFileGenerator) {
        this.snapshotFileGenerator = snapshotFileGenerator;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "NrtmSnapshotFileGenerationTask")
    public void run() {
        try {
            snapshotFileGenerator.createSnapshot();
        } catch (final Exception e) {
            LOGGER.error("NRTM file generation failed", e);
            throw new RuntimeException(e);
        }
    }
}
