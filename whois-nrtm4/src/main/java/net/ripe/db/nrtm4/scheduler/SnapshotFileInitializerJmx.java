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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

@Component
@ManagedResource(objectName = "net.ripe.db.nrtm4:name=SnapshotFileInitializer", description = "Initialize snapshot file")
public class SnapshotFileInitializerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileInitializerJmx.class);
    public static final ScheduledMethodRunnable SNAPSHOT_GENERATION_METHOD = new ScheduledMethodRunnable(SnapshotFileScheduledTask.class, SnapshotFileScheduledTask.class.getDeclaredMethods()[0]);
    private TaskScheduler taskScheduler;
    private final NrtmFileRepository nrtmFileRepository;

    @Autowired
    public SnapshotFileInitializerJmx(final TaskScheduler taskScheduler, final NrtmFileRepository nrtmFileRepository) {
        super(LOGGER);
        this.taskScheduler = taskScheduler;
        this.nrtmFileRepository = nrtmFileRepository;
    }

    @ManagedOperation(description = "Initialize NRTMv4 Database and create initial snapshot file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Comment for invoking the operation")
    })
    public String initializeSnapshotFile(final String comment) {
        return invokeOperation("Initialize snapshot file", comment, () -> {
                nrtmFileRepository.cleanupNrtmv4Database();

                taskScheduler.schedule(SNAPSHOT_GENERATION_METHOD, Instant.now());
                return "Initializing snapshot started";
            });
    }
}
