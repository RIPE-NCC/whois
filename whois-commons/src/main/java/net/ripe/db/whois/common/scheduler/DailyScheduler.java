package net.ripe.db.whois.common.scheduler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.domain.Hosts;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DailyScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyScheduler.class);

    private final DateTimeProvider dateTimeProvider;
    private final DailySchedulerDao dailySchedulerDao;
    private final MaintenanceMode maintenanceMode;
    private List<DailyScheduledTask> scheduledTasks = Collections.emptyList();

    @Autowired
    public DailyScheduler(final DateTimeProvider dateTimeProvider, DailySchedulerDao dailySchedulerDao,
                          final MaintenanceMode maintenanceMode) {
        this.dateTimeProvider = dateTimeProvider;
        this.dailySchedulerDao = dailySchedulerDao;
        this.maintenanceMode = maintenanceMode;
    }

    @Autowired(required = false)
    public void setScheduledTasks(final DailyScheduledTask... scheduledTasks) {
        this.scheduledTasks = Lists.newArrayList(scheduledTasks);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeScheduledTasks() {

        if (!maintenanceMode.allowUpdate()) {
            LOGGER.info("Scheduled tasks not allowed due to maintenance-mode");
            return;
        }

        final LocalDate date = dateTimeProvider.getCurrentDate();
        for (final DailyScheduledTask task : scheduledTasks) {
            if (!dailySchedulerDao.acquireDailyTask(date, task.getClass(), Hosts.getLocalHostName())) {
                continue;
            }

            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                LOGGER.info("Starting scheduled task: {}", task);
                task.run();
                dailySchedulerDao.markTaskDone(System.currentTimeMillis(), date, task.getClass());
            } catch (RuntimeException e) {
                LOGGER.error("Exception in scheduled task: {}", task, e);
            } finally {
                LOGGER.info("Scheduled task: {} took {}", task, stopwatch.stop());
            }
        }

        LOGGER.info("Finished! (no unclaimed tasks left)");
        final int deletedRows = dailySchedulerDao.removeOldScheduledEntries(date);
        LOGGER.info("Purging old entries from scheduler table (key: {}, purged {} old entries)", date, deletedRows);
    }
}
