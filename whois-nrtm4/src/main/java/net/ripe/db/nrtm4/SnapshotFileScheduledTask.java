package net.ripe.db.nrtm4;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
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

    //TODO: do we need to add timezone? or all servers are in UTC now??
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "NrtmSnapshotFileGenerationTask")
    public void run() {
        try {
            snapshotFileGenerator.createSnapshots();
        } catch (final Exception e) {
            LOGGER.error("NRTM file generation failed", e);
            throw new RuntimeException(e);
        }
    }

}
