package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.NrtmConstants;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.generator.SnapshotFileGenerator;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.jdbc.IndexDao;
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
import java.util.Date;
import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "InitializeSnapshotFileNrtmv4", description = "Initialize snapshot file")
public class SnapshotFileInitializerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileInitializerJmx.class);
    @Autowired
    private LockableTaskScheduler taskScheduler;
    @Autowired
    private final SnapshotFileGenerator snapshotFileGenerator;
    @Autowired
    private final NrtmFileRepository nrtmFileRepository;

    @Autowired
    public SnapshotFileInitializerJmx(final LockableTaskScheduler taskScheduler, final NrtmFileRepository nrtmFileRepository, final SnapshotFileGenerator snapshotFileGenerator) {
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
                LOGGER.error("Unable to initalize snapshot file for nrmv4 ", e);
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
