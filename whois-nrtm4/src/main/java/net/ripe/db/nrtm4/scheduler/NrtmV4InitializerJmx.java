package net.ripe.db.nrtm4.scheduler;

import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;


@Component
@Conditional(Nrtmv4Condition.class)
@ManagedResource(objectName = "net.ripe.db.nrtm4:name=NrtmV4Initializer", description = "Initialize snapshot file")
public class NrtmV4InitializerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmV4InitializerJmx.class);
    private final ScheduledMethodRunnable snapshotGenerationTask;
    private final TaskScheduler taskScheduler;
    private final UpdateNrtmFileRepository nrtmFileRepository;

    private final DateTimeProvider dateTimeProvider;


    @Autowired
    public NrtmV4InitializerJmx(final DateTimeProvider dateTimeProvider, @Qualifier("taskScheduler") final TaskScheduler taskScheduler, final SnapshotFileScheduledTask snapshotFileScheduledTask, final UpdateNrtmFileRepository nrtmFileRepository) {
        super(LOGGER);
        this.taskScheduler = taskScheduler;
        this.nrtmFileRepository = nrtmFileRepository;
        this.dateTimeProvider = dateTimeProvider;
        this.snapshotGenerationTask = new ScheduledMethodRunnable(snapshotFileScheduledTask, SnapshotFileScheduledTask.class.getDeclaredMethods()[0]);
    }

    @ManagedOperation(description = "Initialize NRTMv4 Database and create initial snapshot file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Comment for invoking the operation")
    })
    public String runInitializerTask(final String comment) {
        return invokeOperation("Initialize snapshot file", comment, () -> {
               try {
                   nrtmFileRepository.cleanupNrtmv4Database();
               } catch (Exception ex) {
                   LOGGER.error("Error while cleaning NRTM database through jmx due to {}", ex.getMessage());
                   return "unable to clean NRTM database, check whois logs for further info ";
               }

                taskScheduler.schedule(snapshotGenerationTask, dateTimeProvider.getCurrentZonedDateTime().toInstant());
                return "Snapshot file generation task started in background";
            });
    }
}
