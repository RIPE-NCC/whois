package net.ripe.db.nrtm4.scheduler;

import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ManagedResource(objectName = "net.ripe.db.nrtm4:name=SnapshotFileInitializer", description = "Initialize snapshot file")
public class NrtmV4InitializerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmV4InitializerJmx.class);
    private final ScheduledMethodRunnable snapshotGenerationTask;
    private final TaskScheduler taskScheduler;
    private final NrtmFileRepository nrtmFileRepository;

    @Autowired
    public NrtmV4InitializerJmx(final TaskScheduler taskScheduler, final SnapshotFileScheduledTask snapshotFileScheduledTask, final NrtmFileRepository nrtmFileRepository) {
        super(LOGGER);
        this.taskScheduler = taskScheduler;
        this.nrtmFileRepository = nrtmFileRepository;
        this.snapshotGenerationTask = new ScheduledMethodRunnable(snapshotFileScheduledTask, SnapshotFileScheduledTask.class.getDeclaredMethods()[0]);
    }

    @ManagedOperation(description = "Initialize NRTMv4 Database and create initial snapshot file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Comment for invoking the operation")
    })
    public String runInitializerTask(final String comment) {
        return invokeOperation("Initialize snapshot file", comment, () -> {
                nrtmFileRepository.cleanupNrtmv4Database();

                taskScheduler.schedule(snapshotGenerationTask, Instant.now());
                return "Initializing snapshot started";
            });
    }
}
