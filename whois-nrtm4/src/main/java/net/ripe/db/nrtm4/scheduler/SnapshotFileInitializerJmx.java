package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.NrtmConstants;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.generator.SnapshotFileGenerator;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ManagedResource(objectName = "net.ripe.db.nrtm4:name=SnapshotFileInitializer", description = "Initialize snapshot file")
public class SnapshotFileInitializerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileInitializerJmx.class);

    private TaskScheduler taskScheduler;
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final NrtmFileRepository nrtmFileRepository;


    @Autowired
    public SnapshotFileInitializerJmx(final TaskScheduler taskScheduler, final NrtmFileRepository nrtmFileRepository, final SnapshotFileGenerator snapshotFileGenerator) {
        super(LOGGER);
        this.snapshotFileGenerator  = snapshotFileGenerator;
        this.taskScheduler = taskScheduler;
        this.nrtmFileRepository = nrtmFileRepository;
    }

    @ManagedOperation(description = "Initialize NRTMv4 Database and create initial snapshot file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String initializeSnapshotFile(final String comment) {
        return invokeOperation("Initialize snapshot file", comment, () -> {
            try {
                taskScheduler.schedule(() -> scheduledTask(), Instant.now());

                return "Initializing snapshot started";
            } catch (RuntimeException e) {
                LOGGER.error("Unable to initialize snapshot file for nrmv4 ", e);
                return String.format("Unable to recover: %s", e.getMessage());
            }
        });
    }

    @SchedulerLock(name = NrtmConstants.SNAPSHOT_FILE_TASK_NAME)
    private void scheduledTask() {
        nrtmFileRepository.cleanupNrtmv4Database();
        snapshotFileGenerator.createSnapshot();
    }
}
