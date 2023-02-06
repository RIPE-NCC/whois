package net.ripe.db.nrtm4;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class NrtmFileWriterScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileWriterScheduledTask.class);
    private final NrtmFileProcessor nrtmFileProcessor;

    NrtmFileWriterScheduledTask(final NrtmFileProcessor nrtmFileProcessor) {
        this.nrtmFileProcessor = nrtmFileProcessor;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmFileWriterScheduledTask")
    public void run() {
        try {
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        } catch (final IOException e) {
            LOGGER.error("NRTM file generation failed", e);
            throw new RuntimeException(e);
        }
    }

}
