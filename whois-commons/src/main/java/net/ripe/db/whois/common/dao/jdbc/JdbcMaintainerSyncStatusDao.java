package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.MaintainerSyncStatusDao;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcMaintainerSyncStatusDao implements MaintainerSyncStatusDao {

    private final JdbcTemplate internalsTemplate;

    @Autowired
    public JdbcMaintainerSyncStatusDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.internalsTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean isSyncEnabled(CIString mntner) {
            return internalsTemplate.queryForObject(
                    "SELECT count(*)  FROM default_maintainer_sync WHERE mntner = ?",
                   Integer.class,  mntner) > 0;
    }
}
