package net.ripe.db.whois.api.transfer.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
class JdbcTransferUpdateLockDao implements TransferUpdateLockDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTransferUpdateLockDao.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTransferUpdateLockDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void acquireUpdateLock() {
        final String isolationLevel = jdbcTemplate.queryForObject("select @@tx_isolation", String.class);
        if (!isolationLevel.equals("READ-COMMITTED") && !isolationLevel.equals("REPEATABLE-READ") ) {
            throw new IllegalStateException("Invalid isolation level: " + isolationLevel);
        }
        //Any transaction that tries to read the applicable row waits until you are finished.
        //All locks set by FOR UPDATE queries are released when the transaction is committed or rolled back.
        LOGGER.info("Waiting for global transfer lock");
        jdbcTemplate.queryForObject("SELECT global_lock FROM transfer_update_lock WHERE global_lock = 0 FOR UPDATE", Integer.class);
        LOGGER.info("Acquired global transfer lock");
    }
}
