package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.MaintainerSyncStatusDao;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;

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
           try {
               return internalsTemplate.queryForObject(
                       "SELECT is_synchronised FROM default_maintainer_sync_history " +
                               "WHERE mntner = ? ORDER BY timestamp DESC LIMIT 1",
                       (final ResultSet resultSet, final int rowNum) -> resultSet.getBoolean(1),
                       new Object[]{mntner});

           } catch (EmptyResultDataAccessException ex) {
               return false;
           }
    }
}
