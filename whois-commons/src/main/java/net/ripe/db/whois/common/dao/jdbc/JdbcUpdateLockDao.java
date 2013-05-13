package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.UpdateLockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
class JdbcUpdateLockDao implements UpdateLockDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcUpdateLockDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void setUpdateLock() {
        final String isolationLevel = jdbcTemplate.queryForObject("select @@tx_isolation", String.class);
        if (!isolationLevel.equals("READ-COMMITTED")) {
            throw new IllegalStateException("Invalid isolation level: " + isolationLevel);
        }

        jdbcTemplate.queryForInt("SELECT global_lock FROM update_lock WHERE global_lock = 0 FOR UPDATE");
    }
}
