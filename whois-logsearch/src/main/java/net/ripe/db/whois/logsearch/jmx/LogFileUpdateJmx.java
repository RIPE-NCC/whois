package net.ripe.db.whois.logsearch.jmx;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.logsearch.LegacyLogFormatProcessor;
import net.ripe.db.whois.logsearch.LogFileIndex;
import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "LogFileUpdate", description = "Log file update operations")
public class LogFileUpdateJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileUpdateJmx.class);

    private final LogFileIndex logFileIndex;
    private final LegacyLogFormatProcessor legacyLogFormatProcessor;
    private final NewLogFormatProcessor newLogFormatProcessor;

    @Autowired
    public LogFileUpdateJmx(final LogFileIndex logFileIndex, final LegacyLogFormatProcessor legacyLogFormatProcessor, final NewLogFormatProcessor newLogFormatProcessor) {
        super(LOGGER);
        this.logFileIndex = logFileIndex;
        this.legacyLogFormatProcessor = legacyLogFormatProcessor;
        this.newLogFormatProcessor = newLogFormatProcessor;
    }

    @ManagedOperation(description = "Delete index entries by id prefix (not regex, not wildcards; just prefix!)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "idPrefix", description = "prefix (not regex, not wildcards; just prefix!)"),
    })
    public String deleteIndexByIdPrefix(final String idPrefix) {
        invokeOperation("deleteIndexByIdPrefix", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                logFileIndex.removeAllByIdPrefix(idPrefix);
                return null;
            }
        });
        return "Started deleting prefix " + idPrefix;
    }

    @ManagedOperation(description = "Recursively search for legacy log files")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "path", description = "Absolute path for starting recursive lookup"),
    })
    public String indexLegacyDirectory(final String path) {
        if (!new File(path).exists()) {
            return path + " does not exist on the filesystem";
        }

        invokeOperation("index path", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                legacyLogFormatProcessor.addDirectoryToIndex(path);
                return null;
            }
        });
        return "Started indexing " + path;
    }

    @ManagedOperation(description = "Index a single legacy log file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "path", description = "Absolute path to .bz2 file"),
    })
    public String indexLegacyFile(final String path) {
        if (!new File(path).exists()) {
            return path + " does not exist on the filesystem";
        }

        invokeOperation("index path", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                legacyLogFormatProcessor.addFileToIndex(path);
                return null;
            }
        });
        return "Started indexing " + path;
    }

    @ManagedOperation(description = "Recursively search for daily log files (.tar or directory)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "path", description = "Absolute path for starting recursive lookup"),
    })
    public String indexDailyLogDirectory(final String path) {
        if (!new File(path).exists()) {
            return path + " does not exist on the filesystem";
        }

        invokeOperation("index path", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                newLogFormatProcessor.addDirectoryToIndex(path);
                return null;
            }
        });
        return "Started indexing " + path;
    }

    @ManagedOperation(description = "Index a daily log file/dir")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "path", description = "Absolute path to a .tar or daily log directory"),
    })
    public String indexDailyLogFile(final String path) {
        final File file = new File(path);
        if (!file.exists() && !file.isFile()) {
            return path + " is not a valid file path";
        }

        invokeOperation("index path", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                newLogFormatProcessor.addFileToIndex(path);
                return null;
            }
        });
        return "Started indexing " + path;
    }
}
