package net.ripe.db.nrtm4.client.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.processor.UpdateNotificationFileProcessor;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtm4ClientCondition.class)
public class Nrtm4ClientSchedulerTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientSchedulerTask.class);
    private final UpdateNotificationFileProcessor updateNotificationFileProcessor;

    Nrtm4ClientSchedulerTask(final UpdateNotificationFileProcessor updateNotificationFileProcessor) {
        this.updateNotificationFileProcessor = updateNotificationFileProcessor;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "Nrtm4ClientSchedulerTask")
    public void run() {
        LOGGER.info("Started nrtmv4 client");
        try {
            updateNotificationFileProcessor.processFile();
        } catch (final Exception e) {
            LOGGER.error("NRTM update notification file reader failed", e);
            throw new RuntimeException(e);
        }
    }
}
