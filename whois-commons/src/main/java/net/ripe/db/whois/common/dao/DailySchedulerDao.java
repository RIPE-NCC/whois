package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
@RetryFor(RecoverableDataAccessException.class)
@Transactional(isolation = Isolation.READ_COMMITTED)
public class DailySchedulerDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DailySchedulerDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Optional<Timestamp> getDailyTaskFinishTime(final String taskName) {
        try {
            return Optional.of(Timestamp.from(jdbcTemplate.queryForObject(
                    "SELECT lock_until FROM shedlock WHERE name = ?",
                    new Object[] { taskName },
                    java.sql.Timestamp.class
            )));
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

}
