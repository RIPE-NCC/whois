package net.ripe.db.whois.scheduler.task.grs.scheduled;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.scheduler.task.grs.ApnicGrsSource;
import net.ripe.db.whois.scheduler.task.grs.GrsImporter;
import net.ripe.db.whois.scheduler.task.grs.GrsSource;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(GrsScheduledImportCondition.class)
public class ApnicGrsScheduledImport implements DailyScheduledTask {

    private final GrsImporter grsImporter;
    private final GrsSource grsSource;

    public ApnicGrsScheduledImport(final GrsImporter grsImporter,
                                   final ApnicGrsSource grsSource) {
        this.grsImporter = grsImporter;
        this.grsSource = grsSource;
    }

    @Override
    @Scheduled(cron = "0 15 16 * * *", zone = EUROPE_AMSTERDAM)
    @SchedulerLock(name = "ApnicGrsImporter")
    public void run() {
        grsImporter.grsImport(grsSource, false);
    }
}
