package net.ripe.db.whois.scheduler.task.grs.scheduled;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.scheduler.task.grs.AfrinicGrsSource;
import net.ripe.db.whois.scheduler.task.grs.GrsImporter;
import net.ripe.db.whois.scheduler.task.grs.GrsSource;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(GrsScheduledImportCondition.class)
public class AfrinicGrsScheduledImport implements DailyScheduledTask {

    private final GrsImporter grsImporter;
    private final GrsSource grsSource;

    public AfrinicGrsScheduledImport(final GrsImporter grsImporter,
                                     final AfrinicGrsSource grsSource) {
        this.grsImporter = grsImporter;
        this.grsSource = grsSource;
    }

    @Override
    @Scheduled(cron = "0 20 1 * * *")
    @SchedulerLock(name = "AfrinicGrsImporter")
    public void run() {
        grsImporter.grsImport(grsSource, false);
    }
}
