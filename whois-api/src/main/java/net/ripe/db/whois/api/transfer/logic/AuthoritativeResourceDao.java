package net.ripe.db.whois.api.transfer.logic;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
@RetryFor(RecoverableDataAccessException.class)
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AuthoritativeResourceDao {

    private final JdbcTemplate internalsTemplate;

    @Autowired
    public AuthoritativeResourceDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.internalsTemplate = new JdbcTemplate(dataSource);
    }

    public void create(final String source, final String resource) {
        internalsTemplate.update("INSERT INTO authoritative_resource (source, resource) VALUES (?, ?)", source, resource);
    }

    public void delete(final String source, final String resource) {
        internalsTemplate.update("DELETE FROM authoritative_resource WHERE source = ? AND resource = ?", source, resource);
    }
}
