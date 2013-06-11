package net.ripe.db.whois.update.dao;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RetryFor(RecoverableDataAccessException.class)
@Repository
public class JdbcPendingUpdateDao implements PendingUpdateDao {
    private final JdbcTemplate jdbcTemplate;
    private final Joiner COMMA_JOINER = Joiner.on(",");

    @Autowired
    public JdbcPendingUpdateDao(@Qualifier("pendingUpdateDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<PendingUpdate> findByTypeAndKey(final ObjectType type, final String key) {
        return jdbcTemplate.query("" +
                "SELECT id, passed_authentications, object, stored_date " +
                "FROM pending_updates " +
                "WHERE object_type = ? AND pkey = ? " +
                "ORDER BY stored_date ASC",
                new PendingUpdateRowMapper(), ObjectTypeIds.getId(type), key);
    }

    @Override
    public void store(final PendingUpdate pendingUpdate) {
        jdbcTemplate.update("" +
                "INSERT INTO pending_updates(object, object_type, pkey, stored_date, passed_authentications) " +
                "VALUES (?, ?, ?, ?, ?)",
                pendingUpdate.getObject().toByteArray(),
                ObjectTypeIds.getId(pendingUpdate.getObject().getType()),
                pendingUpdate.getObject().getKey(),
                pendingUpdate.getStoredDate().toDate(),
                COMMA_JOINER.join(pendingUpdate.getPassedAuthentications()));
    }

    @Override
    public void remove(final PendingUpdate pendingUpdate) {
        jdbcTemplate.update("DELETE FROM pending_updates WHERE id = ?",
                pendingUpdate.getId());
    }

    @Override
    public List<PendingUpdate> findBeforeDate(final LocalDateTime date) {
        return jdbcTemplate.query("" +
                "SELECT id, passed_authentications, object, stored_date " +
                "FROM pending_updates " +
                "WHERE stored_date < ? ",
                new PendingUpdateRowMapper(), date.toDate());
    }

    private static class PendingUpdateRowMapper implements RowMapper<PendingUpdate> {
        private static final Splitter COMMA_SPLITTER = Splitter.on(",");
        @Override
        public PendingUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new PendingUpdate(
                    rs.getInt("id"),
                    Sets.newHashSet(COMMA_SPLITTER.split(rs.getString("passed_authentications"))),
                    RpslObject.parse(rs.getString("object")),
                    new LocalDateTime(rs.getDate("stored_date"))
            );
        }
    }
}
