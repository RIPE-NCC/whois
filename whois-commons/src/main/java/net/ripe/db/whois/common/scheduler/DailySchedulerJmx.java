package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.DateTimeProvider;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DailyScheduler", description = "Whois daily scheduler")
public class DailySchedulerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailySchedulerJmx.class);

    private final TaskScheduler taskScheduler;
    private final List<DailyScheduledTask> scheduledTasks;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public DailySchedulerJmx(final TaskScheduler taskScheduler,
                             final List<DailyScheduledTask> scheduledTasks,
                             final DateTimeProvider dateTimeProvider) {
        super(LOGGER);
        this.taskScheduler = taskScheduler;
        this.scheduledTasks = scheduledTasks;
        this.dateTimeProvider = dateTimeProvider;
    }

    @ManagedOperation(description = "Run all scheduled tasks")
    public String runDailyScheduledTasks() {
        return invokeOperation("runMaintenance", "", () -> {
            scheduledTasks.stream()
                    .map(task -> taskScheduler.schedule(task::run, dateTimeProvider.getCurrentDateTime().toDate()))
                    .collect(Collectors.toList())
                    .forEach(this::runSilently);
            return "Daily scheduled tasks executed";
        });
    }

    private void runSilently(final ScheduledFuture<?> scheduledFuture) {
        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.info("An exception occurred while runing a job", e);
        }
    }

    @ManagedOperation(description = "Run a specific daily scheduled task (deletes the corresponding entry from scheduler table and reruns all daily tasks)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "className", description = "Name of the DailyScheduledTask to run")
    })
    public String runDailyScheduledTask(final String className) {
        return invokeOperation("runMaintenance", "", () -> {
            Optional<DailyScheduledTask> scheduledTask = scheduledTasks.stream()
                    .filter(task -> task.getClass().getSimpleName().equals(className))
                    .findFirst();

            if (scheduledTask.isPresent()) {
                runSilently(taskScheduler.schedule(() -> scheduledTask.get().run(), dateTimeProvider.getCurrentDateTime().toDate()));
                return "Daily scheduled tasks executed";
            } else {
                return "Class " + className +" not found";
            }
        });
    }
}
