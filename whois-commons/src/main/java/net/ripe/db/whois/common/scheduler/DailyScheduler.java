package net.ripe.db.whois.common.scheduler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.Hosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Component
public class DailyScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyScheduler.class);

    private final DateTimeProvider dateTimeProvider;
    private final JdbcTemplate internalsTemplate;
    private List<DailyScheduledTask> scheduledTasks = Collections.emptyList();

    @Autowired
    public DailyScheduler(final DateTimeProvider dateTimeProvider, @Qualifier("internalsDataSource") final DataSource internalsDataSource) {
        this.dateTimeProvider = dateTimeProvider;
        this.internalsTemplate = new JdbcTemplate(internalsDataSource);
    }

    @Autowired(required = false)
    public void setScheduledTasks(final DailyScheduledTask... scheduledTasks) {
        this.scheduledTasks = Lists.newArrayList(scheduledTasks);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeScheduledTasks() {
        final String date = dateTimeProvider.getCurrentDate().toString();
        final String hostName = Hosts.getLocalHost().getHostName();

        for (final DailyScheduledTask task : scheduledTasks) {
            final String taskName = task.getClass().getSimpleName();
            if (!acquireDailyTask(date, taskName, hostName)) {
                continue;
            }

            final Stopwatch stopwatch = new Stopwatch().start();
            try {
                LOGGER.debug("Starting scheduled task: {}", task);
                task.run();
                internalsTemplate.update("UPDATE scheduler SET done = ? WHERE date = ? AND task = ?", System.currentTimeMillis(), date, taskName);
            } catch (RuntimeException e) {
                LOGGER.error("Exception in scheduled task: {}", task, e);
            } finally {
                LOGGER.info("Scheduled task: {} took {}", task, stopwatch.stop());
            }
        }

        final int deletedRows = internalsTemplate.update("DELETE FROM scheduler WHERE date < ?", date);
        LOGGER.info("Performing daily cluster maintenance (key: {}, purged {} old entries)", date, deletedRows);
    }

    public long getDailyTaskFinishTime(Class taskClass) {
        final String date = dateTimeProvider.getCurrentDate().toString();
        final String taskName = taskClass.getSimpleName();
        final List<Long> result = internalsTemplate.queryForList("SELECT done FROM scheduler WHERE date = ? AND task = ?", Long.class, date, taskName);
        return result.isEmpty() ? -1 : result.get(0);
    }

    public void removeFinishedScheduledTasks() {
        internalsTemplate.update("TRUNCATE scheduler");
    }

    public void removeFinishedScheduledTask(Class taskClass) {
        final String taskName = taskClass.getSimpleName();
        internalsTemplate.update("DELETE FROM scheduler WHERE task = ?", taskName);
    }

    private boolean acquireDailyTask(final String date, final String taskName, final String hostName) {
        try {
            internalsTemplate.update("INSERT INTO scheduler (date, task, host) VALUES (?, ?, ?)", date, taskName, hostName);
        } catch (NonTransientDataAccessException ignored) {
            LOGGER.debug("Scheduled task already run on different cluster member");
            return false;
        }
        return true;
    }
}
