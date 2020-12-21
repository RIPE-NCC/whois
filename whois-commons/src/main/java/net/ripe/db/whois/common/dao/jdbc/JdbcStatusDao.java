package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcStatusDao implements StatusDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcStatusDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public CIString getStatus(final int objectId) {
        return jdbcTemplate.queryForObject(
            "SELECT status FROM status WHERE object_id = ?",
            (rs, rowNum) -> CIString.ciString(rs.getString(1)),
            objectId);
    }

}
