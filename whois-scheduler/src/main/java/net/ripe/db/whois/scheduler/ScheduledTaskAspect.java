package net.ripe.db.whois.scheduler;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Configurable
public class ScheduledTaskAspect {

    private final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskAspect.class);

    private MaintenanceMode maintenanceMode;

    @Around("this(net.ripe.db.whois.common.scheduler.DailyScheduledTask) && @annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();

        if (target instanceof DailyScheduledTask) {
            final Stopwatch stopwatch = Stopwatch.createStarted();

            LOGGER.info("Starting scheduled task: {}", target);

            if (maintenanceMode != null && !maintenanceMode.allowUpdate()) {
                LOGGER.info("Scheduled tasks not allowed due to maintenance-mode");
            } else {
                joinPoint.proceed();
            }

            LOGGER.info("Scheduled task: {} took {}", target, stopwatch.stop());
        }
    }

    @Autowired
    public void setMaintenanceMode(final MaintenanceMode maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

}
