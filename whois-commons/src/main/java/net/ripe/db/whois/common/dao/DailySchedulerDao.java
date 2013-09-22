package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
@RetryFor(RecoverableDataAccessException.class)
@Transactional(isolation = Isolation.READ_COMMITTED)
public class DailySchedulerDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailySchedulerDao.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DailySchedulerDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void markTaskDone(final long when, final LocalDate date, final Class taskClass) {
        jdbcTemplate.update("UPDATE scheduler SET done = ? WHERE date = ? AND task = ?", when/1000, date.toString(), taskClass.getSimpleName());
    }

    public long getDailyTaskFinishTime(final LocalDate date, final Class taskClass) {
        final List<Long> result = jdbcTemplate.queryForList("SELECT done FROM scheduler WHERE date = ? AND task = ?", Long.class, date.toString(), taskClass.getSimpleName());
        return result.isEmpty() ? -1 : result.get(0) * 1000;
    }

    public void removeFinishedScheduledTasks() {
        jdbcTemplate.update("TRUNCATE scheduler");
    }

    public void removeFinishedScheduledTask(final Class taskClass) {
        jdbcTemplate.update("DELETE FROM scheduler WHERE task = ?", taskClass.getSimpleName());
    }

    public int removeOldScheduledEntries(final LocalDate date) {
        return jdbcTemplate.update("DELETE FROM scheduler WHERE date < ?", date.toString());
    }

    public boolean acquireDailyTask(final LocalDate date, final Class taskClass, final String hostName) {
        try {
            jdbcTemplate.update("INSERT INTO scheduler (date, task, host) VALUES (?, ?, ?)", date.toString(), taskClass.getSimpleName(), hostName);
        } catch (NonTransientDataAccessException ignored) {
            LOGGER.debug("Scheduled task already run on different cluster member", ignored);
            return false;
        }
        return true;
    }
}
