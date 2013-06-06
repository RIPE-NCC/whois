package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.update.domain.PendingUpdate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcPendingUpdateDao implements PendingUpdateDao {
    private final JdbcTemplate jdbcTemplate;

    // TODO [AK] The data source is not pending, but points to a database containing deferred updates, rename to e.g. deferredUpdateSource
    @Autowired
    public JdbcPendingUpdateDao(@Qualifier("pendingDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<PendingUpdate> findByTypeAndKey(final ObjectType type, final String key) {
        return jdbcTemplate.query("SELECT * FROM pending_updates WHERE object_type = ? AND pkey = ? ORDER BY stored_date ASC",
                new RowMapper<PendingUpdate>() {
            @Override
            public PendingUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {

                // TODO [AK] Don't use select(*). In general we specify which columns to query and refer to them in the rs using indexes
                return new PendingUpdate(
                        rs.getString("authenticated_by"), // TODO [AK] This should be any collection and split here
                        RpslObjectBase.parse(rs.getString("object")),
                        new LocalDateTime(rs.getDate("stored_date"))
                );
            }
        }, ObjectTypeIds.getId(type), key);
    }

    @Override
    public void store(final PendingUpdate pendingUpdate) {
        jdbcTemplate.update("" +
                "INSERT INTO pending_updates(object, object_type, pkey, stored_date, authenticated_by) " +
                "VALUES (?, ?, ?, ?, ?)",
                pendingUpdate.getObject().toString(),
                ObjectTypeIds.getId(pendingUpdate.getObject().getType()),
                pendingUpdate.getObject().getKey(),
                pendingUpdate.getStoredDate().toDate(),
                pendingUpdate.getAuthenticatedBy()); // TODO [AK] this should be any collection and joined here
    }

    @Override // TODO [AK] In general our remove methods take ids
    public void remove(final PendingUpdate pendingUpdate) {
        // TODO [AK] This is not correct!! does not take into account if an object is identical. It will blindly remove the first one.
        // TODO [AK] pending_updates should have an auto generated id, so we know which one to delete.
        jdbcTemplate.update("DELETE FROM pending_updates WHERE object_type = ? AND pkey = ? ORDER BY stored_date ASC LIMIT 1",
                ObjectTypeIds.getId(pendingUpdate.getObject().getType()),
                pendingUpdate.getObject().getKey().toString());
    }

    @Override
    public void removePendingUpdatesBefore(final LocalDateTime date) {
        jdbcTemplate.update("DELETE FROM pending_updates WHERE stored_date < ?", date.toDate());
    }
}
