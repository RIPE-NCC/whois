package net.ripe.db.whois.scheduler;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.DateUtil.toDate;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DailyScheduler", description = "Whois daily scheduler")
public class DailySchedulerJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailySchedulerJmx.class);

    private final TaskScheduler taskScheduler;
    private Map<String, ScheduledMethodRunnable> scheduledTasks;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public DailySchedulerJmx(@Qualifier("taskScheduler") final TaskScheduler taskScheduler,
                             final DateTimeProvider dateTimeProvider,
                             final List<DailyScheduledTask> scheduledTasks) {
        super(LOGGER);
        this.taskScheduler = taskScheduler;
        this.dateTimeProvider = dateTimeProvider;
        this.scheduledTasks = createScheduledMethodRunnables(scheduledTasks);
    }

    /**
     * Need to wrap all our {@DailyScheduledTask}s in {@link ScheduledMethodRunnable}
     * as these are expected by Spring's task scheduler's AOP proxies.
     * @param scheduledTasks list of {@link DailyScheduledTask}s to wrap
     * @return a map with wrapped ScheduledMethodRunnables indexed by job name
     */
    private Map<String, ScheduledMethodRunnable> createScheduledMethodRunnables(final List<DailyScheduledTask> scheduledTasks) {
        final Map<String, ScheduledMethodRunnable> scheduled = Maps.newHashMap();
        for (DailyScheduledTask scheduledTask : scheduledTasks) {
            Class<?> clazz = AopUtils.getTargetClass(scheduledTask);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Scheduled.class)) {
                    scheduled.put(clazz.getSimpleName(), new ScheduledMethodRunnable(scheduledTask, method));
                }
            }
        }
        return scheduled;
    }

    @ManagedOperation(description = "Run all scheduled tasks")
    public String runDailyScheduledTasks() {
        return invokeOperation("runMaintenance", "", () -> {
            scheduledTasks.values().stream()
                    .map(task -> taskScheduler.schedule(task, toDate(dateTimeProvider.getCurrentDateTime())))
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
            ScheduledMethodRunnable scheduledTask = scheduledTasks.get(className);

            if (scheduledTask != null) {
                runSilently(taskScheduler.schedule(scheduledTask, toDate(dateTimeProvider.getCurrentDateTime())));
                return "Daily scheduled tasks executed";
            } else {
                return "Class " + className +" not found";
            }
        });
    }

}
