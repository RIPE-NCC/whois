package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DailyScheduler", description = "Whois daily scheduler")
public class DailySchedulerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailySchedulerJmx.class);

    private final DailyScheduler dailyScheduler;
    private final DailySchedulerDao dailySchedulerDao;

    @Autowired
    public DailySchedulerJmx(final DailyScheduler dailyScheduler, DailySchedulerDao dailySchedulerDao) {
        super(LOGGER);
        this.dailyScheduler = dailyScheduler;
        this.dailySchedulerDao = dailySchedulerDao;
    }

    @ManagedOperation(description = "Run daily scheduled tasks that are not marked as done in the scheduler table")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "force", description = "truncate the scheduler table before running scheduled tasks")
    })
    public String runDailyScheduledTasks(final boolean force) {
        return invokeOperation("runMaintenance", "", new Callable<String>() {
            @Override
            public String call() {
                if (force) dailySchedulerDao.removeFinishedScheduledTasks();
                dailyScheduler.executeScheduledTasks();
                return "Daily scheduled tasks executed";
            }
        });
    }

    @ManagedOperation(description = "Run a specific daily scheduled task (deletes the corresponding entry from scheduler table and reruns all daily tasks)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "className", description = "Name of the DailyScheduledTask to run")
    })
    public String runDailyScheduledTask(final String className) {
        return invokeOperation("runMaintenance", "", new Callable<String>() {
            @Override
            public String call() {
                final Class<?> taskClass;
                try {
                    taskClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    return "Class " + className +" not found";
                }
                dailySchedulerDao.removeFinishedScheduledTask(taskClass);
                dailyScheduler.executeScheduledTasks();
                return "Daily scheduled tasks executed";
            }
        });
    }
}
