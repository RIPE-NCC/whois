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

    /**
     * The time zone for this job is set to EUROPE_AMSTERDAM.
     * This ensures the files it generates on the FTP server remains
     * being generated at midnight, Amsterdam time regardless of switch to UTC
     */
    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = EUROPE_AMSTERDAM)
    @SchedulerLock(name = "DatabaseTextExport")
    public void run() {
        rpslObjectsExporter.export();
    }
}
