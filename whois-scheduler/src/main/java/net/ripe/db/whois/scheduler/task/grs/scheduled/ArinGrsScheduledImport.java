package net.ripe.db.whois.scheduler.task.grs.scheduled;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.scheduler.task.grs.ArinGrsSource;
import net.ripe.db.whois.scheduler.task.grs.GrsImporter;
import net.ripe.db.whois.scheduler.task.grs.GrsSource;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(GrsScheduledImportCondition.class)
public class ArinGrsScheduledImport implements DailyScheduledTask {

    private final GrsImporter grsImporter;
    private final GrsSource grsSource;

    public ArinGrsScheduledImport(final GrsImporter grsImporter,
                                  final ArinGrsSource grsSource) {
        this.grsImporter = grsImporter;
        this.grsSource = grsSource;
    }

    @Override
    @Scheduled(cron = "0 15 15 * * *", zone = EUROPE_AMSTERDAM)
    @SchedulerLock(name = "ArinGrsImporter")
    public void run() {
        grsImporter.grsImport(grsSource, false);
    }
}
