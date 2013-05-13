package net.ripe.db.whois.scheduler;

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
    private final JdbcTemplate schedulerTemplate;
    private List<DailyScheduledTask> scheduledTasks = Collections.emptyList();

    @Autowired
    public DailyScheduler(final DateTimeProvider dateTimeProvider, @Qualifier("schedulerDataSource") final DataSource schedulerDataSource) {
        this.dateTimeProvider = dateTimeProvider;
        this.schedulerTemplate = new JdbcTemplate(schedulerDataSource);
    }

    @Autowired(required = false)
    public void setScheduledTasks(final DailyScheduledTask... scheduledTasks) {
        this.scheduledTasks = Lists.newArrayList(scheduledTasks);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeScheduledTasks() {
        attemptScheduledTasks();
    }

    boolean attemptScheduledTasks() {
        final String date = dateTimeProvider.getCurrentDate().toString();

        try {
            schedulerTemplate.update("INSERT INTO scheduler VALUES (?, ?)", date, Hosts.getLocalHost().getHostName());
        } catch (NonTransientDataAccessException ignored) {
            LOGGER.debug("Scheduled task already run on different cluster member");
            return false;
        }

        final int deletedRows = schedulerTemplate.update("DELETE FROM scheduler WHERE date < ?", date);
        LOGGER.info("Performing daily cluster maintenance (key: {}, purged {} old entries)", date, deletedRows);
        runScheduledTasks();
        return true;
    }

    void runScheduledTasks() {
        new Thread("DailyScheduler-TaskRunner") {
            @Override
            public void run() {
                for (final DailyScheduledTask task : scheduledTasks) {
                    final Stopwatch stopwatch = new Stopwatch().start();
                    try {
                        LOGGER.debug("Starting scheduled task: {}", task);
                        task.run();
                    } catch (RuntimeException e) {
                        LOGGER.error("Exception in scheduled task: {}", task, e);
                    } finally {
                        LOGGER.info("Scheduled task: {} took {}", task, stopwatch.stop());
                    }
                }
            }
        }.start();
    }
}
