package net.ripe.db.nrtm4;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Conditional(Nrtmv4Condition.class)
public class NrtmDeltaFileScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmDeltaFileScheduledTask.class);
    final DeltaFileGenerator deltaFileGenerator;

    NrtmDeltaFileScheduledTask(final DeltaFileGenerator deltaFileGenerator) {
        this.deltaFileGenerator = deltaFileGenerator;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmDeltaFileWriterScheduledTask")
    public void run() {
        LOGGER.info("Started delta file generation");
        try {
            deltaFileGenerator.createDeltas();
        } catch (final Exception e) {
            LOGGER.error("NRTM delta file generation failed", e);
            throw new RuntimeException(e);
        }
    }

}
