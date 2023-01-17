package net.ripe.db.nrtm4;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class NrtmFileWriterScheduledTask implements DailyScheduledTask {

    private final NrtmFileProcessor nrtmFileProcessor;

    NrtmFileWriterScheduledTask(final NrtmFileProcessor nrtmFileProcessor) {
        this.nrtmFileProcessor = nrtmFileProcessor;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmFileWriterScheduledTask")
    public void run() {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
    }

}
