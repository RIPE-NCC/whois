package net.ripe.db.nrtm4.client.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.client.scheduler.reader.UpdateNotificationFileReader;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtmv4ClientCondition.class)
public class Nrtm4ClientSchedulerTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientSchedulerTask.class);
    private final UpdateNotificationFileReader updateNotificationFileGenerator;

    Nrtm4ClientSchedulerTask(final UpdateNotificationFileReader updateNotificationFileGenerator) {
        this.updateNotificationFileGenerator = updateNotificationFileGenerator;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmNotificationFileReaderTask")
    public void run() {
        try {
            updateNotificationFileGenerator.readFile();
        } catch (final Exception e) {
            LOGGER.error("NRTM update notification file reader failed", e);
            throw new RuntimeException(e);
        }
    }
}