package net.ripe.db.whois.api.wsearch;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "LogFileSearch", description = "Log file search")
public class LogFileSearchJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileSearchJmx.class);

    private final LogFileIndex logFileIndex;

    @Autowired
    public LogFileSearchJmx(final LogFileIndex logFileIndex) {
        super(LOGGER);
        this.logFileIndex = logFileIndex;
    }

    @ManagedOperation(description = "Update log file index")
    public String incrementalImport() {
        invokeOperation("log/update", "", new Callable<Void>() {
            @Override
            public Void call() {
                logFileIndex.update();
                return null;
            }
        });

        return "Update started";
    }

    @ManagedOperation(description = "Rebuild log file index")
    public String fullImport() {
        invokeOperation("rebuild", "", new Callable<Void>() {
            @Override
            public Void call() {
                logFileIndex.rebuild();
                return null;
            }
        });

        return "Update started";
    }
}
