package net.ripe.db.whois.scheduler;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DailyScheduler", description = "Whois daily scheduler")
public class DailySchedulerJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailySchedulerJmx.class);

    private final DailyScheduler dailyScheduler;

    @Autowired
    public DailySchedulerJmx(final DailyScheduler dailyScheduler) {
        super(LOGGER);
        this.dailyScheduler = dailyScheduler;
    }

    @ManagedOperation(description = "Run daily scheduled tasks if for some reason they were not scheduled yet")
    public String runDailyScheduledTasks() {
        return invokeOperation("runMaintenance", "", new Callable<String>() {
            @Override
            public String call() {
                if (dailyScheduler.attemptScheduledTasks()) {
                    return "Daily scheduled tasks executed";
                } else {
                    return "Skipped: Daily scheduled tasks already executed";
                }
            }
        });
    }

    @ManagedOperation(description = "Run daily scheduled tasks in any case")
    public String runDailyScheduledTasksAlways() {
        return invokeOperation("runMaintenanceAlways", "", new Callable<String>() {
            @Override
            public String call() {
                dailyScheduler.runScheduledTasks();
                return "Daily scheduled tasks executed";
            }
        });
    }
}
