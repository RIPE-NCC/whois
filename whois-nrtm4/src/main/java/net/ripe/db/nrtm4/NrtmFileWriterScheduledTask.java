package net.ripe.db.nrtm4;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class NrtmFileWriterScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileWriterScheduledTask.class);
    private final NrtmFileProcessor nrtmFileProcessor;
    private final Boolean enableNrtm4;

    NrtmFileWriterScheduledTask(
        final NrtmFileProcessor nrtmFileProcessor,
        @Value("${nrtm4.enabled:true}") final Boolean enableNrtm4
    ) {
        this.nrtmFileProcessor = nrtmFileProcessor;
        this.enableNrtm4 = enableNrtm4;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmFileWriterScheduledTask")
    public void run() {
        if (!enableNrtm4) {
            return;
        }
        try {
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        } catch (final Exception e) {
            LOGGER.error("NRTM file generation failed", e);
            throw new RuntimeException(e);
        }
    }

}
