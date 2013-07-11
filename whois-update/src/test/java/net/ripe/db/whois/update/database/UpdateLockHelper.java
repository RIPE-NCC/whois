package net.ripe.db.whois.update.database;

import net.ripe.db.whois.common.dao.UpdateLockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
public class UpdateLockHelper {

    private UpdateLockDao updateLockDao;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UpdateLockHelper(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final UpdateLockDao updateLockDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.updateLockDao = updateLockDao;
    }

    private void setMntnerValue(final String value) {
        jdbcTemplate.update("UPDATE mntner SET mntner = ?", value.getBytes());
    }

    private String getMntnerValue() {
        return jdbcTemplate.queryForObject("SELECT mntner FROM mntner WHERE object_id = 1", String.class);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void testUpdateLock() {
        jdbcTemplate.queryForInt("SELECT count(*) FROM mntner");

        // if setting update lock is not the first select after transaction starts,
        // other transactions won't see changes if using repeatable_read isolation
        updateLockDao.setUpdateLock();

        setMntnerValue(getMntnerValue() + ".");
    }
}
