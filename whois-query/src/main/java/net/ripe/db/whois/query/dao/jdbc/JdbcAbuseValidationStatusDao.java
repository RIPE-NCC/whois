package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.query.dao.AbuseValidationStatusDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
    public boolean isSuspect(CIString email) {
        return internalsTemplate.queryForObject(
                "select count(*) from abuse_email where address = ? and status = ?",
                new String[] { email.toLowerCase(), "SUSPECT" },
                Integer.class) > 0;
    }
}
