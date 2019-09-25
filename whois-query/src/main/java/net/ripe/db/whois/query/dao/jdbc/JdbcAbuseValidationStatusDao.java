package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.query.dao.AbuseValidationStatusDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcAbuseValidationStatusDao implements AbuseValidationStatusDao {

    private final JdbcTemplate internalsTemplate;

    @Autowired
    public JdbcAbuseValidationStatusDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.internalsTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean isSuspect(@Nullable final CIString address) {
        return (address != null) && internalsTemplate.queryForObject(
                "SELECT count(*) FROM abuse_email WHERE address = ? AND status = ?",
                new Object[] { address.toString(), "SUSPECT" },
                Integer.class) > 0;
    }
}
