package net.ripe.db.whois.scheduler.task.export;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTextExport implements DailyScheduledTask {
    private final RpslObjectsExporter rpslObjectsExporter;

    @Autowired
    public DatabaseTextExport(final RpslObjectsExporter rpslObjectsExporter) {
        this.rpslObjectsExporter = rpslObjectsExporter;
    }

    @Override
    @Scheduled(cron = "0 0 19 * * *", zone = RUN_TIMEZONE)
    @SchedulerLock(name = "DatabaseTextExport")
    public void run() {
        rpslObjectsExporter.export();
    }
}
