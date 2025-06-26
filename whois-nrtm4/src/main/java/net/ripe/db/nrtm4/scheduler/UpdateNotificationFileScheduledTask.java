package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.generator.UpdateNotificationFileGenerator;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtmv4Condition.class)
public class UpdateNotificationFileScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileScheduledTask.class);
    private final UpdateNotificationFileGenerator updateNotificationFileGenerator;

    UpdateNotificationFileScheduledTask(final UpdateNotificationFileGenerator updateNotificationFileGenerator) {
        this.updateNotificationFileGenerator = updateNotificationFileGenerator;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmNotificationFileGenerationTask")
    public void run() {
        try {
            updateNotificationFileGenerator.generateFile();
        } catch (final Exception e) {
            LOGGER.error("NRTM file generation failed", e);
            throw new RuntimeException(e);
        }
    }

}
