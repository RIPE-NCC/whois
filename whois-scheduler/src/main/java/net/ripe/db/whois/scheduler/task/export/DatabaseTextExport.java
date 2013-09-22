package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTextExport implements DailyScheduledTask {
    private final RpslObjectsExporter rpslObjectsExporter;

    @Autowired
    public DatabaseTextExport(final RpslObjectsExporter rpslObjectsExporter) {
        this.rpslObjectsExporter = rpslObjectsExporter;
    }

    @Override
    public void run() {
        rpslObjectsExporter.export();
    }
}
