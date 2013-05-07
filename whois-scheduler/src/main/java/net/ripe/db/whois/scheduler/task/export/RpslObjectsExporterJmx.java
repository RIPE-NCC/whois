package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DatabaseExporter", description = "Whois database exporter")
public class RpslObjectsExporterJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectsExporterJmx.class);

    private final RpslObjectsExporter objectsExporter;

    @Autowired
    public RpslObjectsExporterJmx(final RpslObjectsExporter objectsExporter) {
        super(LOGGER);
        this.objectsExporter = objectsExporter;
    }

    @ManagedOperation(description = "Create text export of whois database in configured destination")
    public String exportDatabase() {
        return invokeOperation("Export database", "", new Callable<String>() {
            @Override
            public String call() {
                new DatabaseExporterThread(objectsExporter).start();
                return "Export started";
            }
        });
    }

    private static class DatabaseExporterThread extends Thread {
        private final RpslObjectsExporter objectsExporter;

        private DatabaseExporterThread(final RpslObjectsExporter objectsExporter) {
            super("Database exporter");
            this.objectsExporter = objectsExporter;
        }

        @Override
        public void run() {
            try {
                objectsExporter.export();
            } catch (RuntimeException e) {
                LOGGER.error("Exporting database in background", e);
            }
        }
    }
}
